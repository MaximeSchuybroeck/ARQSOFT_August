package com.example.lending_service.service;

import com.example.lending_service.client.Client;
import com.example.lending_service.entity.Lending;
import com.example.lending_service.entity.Recommendation;
import com.example.lending_service.entity.Recommendation.Sentiment;
import com.example.lending_service.repository.LendingRepository;
import com.example.lending_service.repository.RecommendationRepository;
import com.example.lending_service.dto.ReturnRequest;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class LendingService {

    private final LendingRepository repo;
    private final RecommendationRepository recRepo;
    private final Client.BookClient bookClient;
    private final Client.ReaderClient readerClient;
    private final RabbitTemplate rabbitTemplate;

    @Value("${book.service.url:http://book-service:8083}")
    private String bookServiceBaseUrl;

    public LendingService(
            LendingRepository repo,
            RecommendationRepository recRepo,
            Client.BookClient bookClient,
            Client.ReaderClient readerClient,
            RabbitTemplate rabbitTemplate
    ) {
        this.repo = repo;
        this.recRepo = recRepo;
        this.bookClient = bookClient;
        this.readerClient = readerClient;
        this.rabbitTemplate = rabbitTemplate;
    }

    // -------- Borrow --------
    @Transactional
    public LendingDTO borrow(Long bookId, String readerEmail, Integer days) {
        Objects.requireNonNull(bookId, "bookId is required");
        Objects.requireNonNull(readerEmail, "readerEmail is required");

        if (!bookClient.bookExists(bookId)) {
            throw new IllegalArgumentException("Book " + bookId + " does not exist.");
        }
        if (!readerClient.readerExists(readerEmail)) {
            throw new IllegalArgumentException("Reader " + readerEmail + " does not exist.");
        }

        Optional<Lending> active = repo.findByBookIdAndReturnedDateIsNull(bookId);
        if (active.isPresent()) {
            throw new IllegalStateException("Book " + bookId + " is already lent.");
        }

        LocalDate start = LocalDate.now();
        int lendDays = (days == null || days <= 0) ? 14 : days;
        LocalDate due = start.plusDays(lendDays);

        Lending l = new Lending();
        l.setBookId(bookId);
        l.setReaderEmail(readerEmail);
        l.setStartDate(start);
        l.setDueDate(due);
        l = repo.save(l);

        safePublish("lending.exchange", "lending.book-borrowed",
                new LendingEvent(l.getId(), l.getBookId(), l.getReaderEmail(), "BORROWED"));

        return toDTO(l);
    }

    // -------- Return (by lending id) --------
    @Transactional
    public LendingDTO returnById(Long lendingId, Boolean recommended, String comment) {
        // Only allow active lending to be returned
        Optional<Lending> optActive = repo.findByIdAndReturnedDateIsNull(lendingId);
        if (optActive.isEmpty()) {
            // Differentiate between "already returned" vs "no such lending"
            if (repo.findById(lendingId).isPresent()) {
                throw new IllegalStateException("Lending " + lendingId + " was already returned.");
            } else {
                throw new IllegalArgumentException("Lending " + lendingId + " not found.");
            }
        }

        Lending lending = optActive.get();
        doReturnAndMaybeRecommend(lending, recommended, comment);
        return toDTO(lending);
    }

    // -------- Return (by reader + bookId) --------
    @Transactional
    public LendingDTO returnByReaderAndBook(ReturnRequest req, Boolean recommended, String comment) {
        Objects.requireNonNull(req.getBookId(), "bookId is required");
        Objects.requireNonNull(req.getReaderEmail(), "readerEmail is required");

        Lending lending = repo.findByBookIdAndReaderEmailAndReturnedDateIsNull(req.getBookId(), req.getReaderEmail())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No active lending found for reader " + req.getReaderEmail() + " and book " + req.getBookId()
                ));

        doReturnAndMaybeRecommend(lending, recommended, comment);
        return toDTO(lending);
    }

    // -------- Return (by reader + bookTitle) --------
    @Transactional
    public LendingDTO returnByReaderAndBookTitle(String readerEmail, String bookTitle, Boolean recommended, String comment) {
        Objects.requireNonNull(readerEmail, "readerEmail is required");
        Objects.requireNonNull(bookTitle, "bookTitle is required");

        Long bookId = resolveBookIdByTitle(bookTitle);
        if (bookId == null) {
            throw new IllegalArgumentException("Could not resolve book title '" + bookTitle + "' to a book id.");
        }

        Lending lending = repo.findByBookIdAndReaderEmailAndReturnedDateIsNull(bookId, readerEmail)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No active lending found for reader " + readerEmail + " and book '" + bookTitle + "' (id=" + bookId + ")"
                ));

        doReturnAndMaybeRecommend(lending, recommended, comment);
        return toDTO(lending);
    }

    // Core return + recommendation logic (single transaction)
    private void doReturnAndMaybeRecommend(Lending lending, Boolean recommended, String comment) {
        // Hard guard (idempotency)
        if (lending.getReturnedDate() != null) {
            throw new IllegalStateException("This lending was already returned.");
        }

        lending.setReturnedDate(LocalDate.now());
        repo.save(lending); // mark as returned

        if (recommended != null) {
            // Avoid duplicate recommendations per lending
            if (recRepo.existsByLendingId(lending.getId())) {
                throw new IllegalStateException("A recommendation already exists for lending " + lending.getId() + ".");
            }

            Recommendation r = new Recommendation();
            r.setBookId(lending.getBookId());
            r.setLendingId(lending.getId());
            r.setReaderEmail(lending.getReaderEmail()); // ensure not-null and correct aggregation
            r.setSentiment(Boolean.TRUE.equals(recommended) ? Sentiment.POSITIVE : Sentiment.NEGATIVE);
            r.setComment(comment);
            recRepo.save(r);
        }

        safePublish("lending.exchange", "lending.book-returned",
                new LendingEvent(lending.getId(), lending.getBookId(), lending.getReaderEmail(), "RETURNED"));
    }

    // -------- Queries --------
    public List<LendingDTO> getAllLendings() {
        return repo.findAll().stream().map(this::toDTO).toList();
    }

    public List<LendingDTO> getOverdueLendings() {
        return repo.findOverdueLendings().stream().map(this::toDTO).toList();
    }
    public List<LendingDTO> getActiveLendings() {
        return repo.findByReturnedDateIsNull().stream().map(this::toDTO).toList();
    }

    public List<LendingDTO> getActiveLendingsByReader(String readerEmail) {
        return repo.findByReaderEmailAndReturnedDateIsNull(readerEmail).stream().map(this::toDTO).toList();
    }

    public Double getAverageLendingDuration() {
        Double avg = repo.averageLendingDuration();
        return avg == null ? 0.0 : avg;
    }

    public RecommendationSummaryDTO getRecommendationSummary(Long bookId) {
        long positives = recRepo.countByBookIdAndSentiment(bookId, Sentiment.POSITIVE);
        long negatives = recRepo.countByBookIdAndSentiment(bookId, Sentiment.NEGATIVE);
        long total = recRepo.countByBookId(bookId);
        double ratio = total == 0 ? 0.0 : ((double) positives) / total;

        RecommendationSummaryDTO dto = new RecommendationSummaryDTO();
        dto.setBookId(bookId);
        dto.setPositives(positives);
        dto.setNegatives(negatives);
        dto.setTotal(total);
        dto.setPositiveRatio(ratio);
        return dto;
    }

    // -------- Helpers --------
    private LendingDTO toDTO(Lending l) {
        LendingDTO dto = new LendingDTO();
        dto.setId(l.getId());
        dto.setBookId(l.getBookId());
        dto.setReaderEmail(l.getReaderEmail());
        dto.setStartDate(l.getStartDate());
        dto.setDueDate(l.getDueDate());
        dto.setReturnedDate(l.getReturnedDate());
        return dto;
    }

    private void safePublish(String exchange, String routingKey, Object payload) {
        try {
            if (rabbitTemplate != null) {
                rabbitTemplate.convertAndSend(exchange, routingKey, payload);
            }
        } catch (Exception ignored) { }
    }

    private static final Logger log = LoggerFactory.getLogger(LendingService.class);

    /**
     * Resolve a book id by title using book-service. Tries several common endpoints and
     * finally GETs /api/books and filters locally (case-insensitive exact match).
     * Returns null if not found. Logs details to help you diagnose connectivity/mapping.
     *
     * Make sure the property 'book.service.url' points to a reachable base URL from this service,
     * e.g.:
     *   book.service.url=http://book-service:8083          (compose service name)
     *   book.service.url=http://host.docker.internal:8083  (book-service on host, lending in Docker)
     *   book.service.url=http://localhost:8083             (both running on host, not in Docker)
     */
    private Long resolveBookIdByTitle(String title) {
        String enc = URLEncoder.encode(title, StandardCharsets.UTF_8);
        String base = bookServiceBaseUrl.endsWith("/") ? bookServiceBaseUrl.substring(0, bookServiceBaseUrl.length() - 1)
                : bookServiceBaseUrl;

        String[] paths = new String[] {
                "/api/books/by-title?title=" + enc,  // if you later add it
                "/api/books/search?title=" + enc,    // if you expose a search endpoint
                "/api/books?title=" + enc,           // some services support filtering
                "/api/books"                          // fallback: list then filter here
        };

        HttpClient http = HttpClient.newHttpClient();
        ObjectMapper om = new ObjectMapper();

        for (String p : paths) {
            String url = base + p;
            try {
                HttpRequest req = HttpRequest.newBuilder(URI.create(url)).GET().build();
                HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());

                if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
                    log.warn("Title resolve: {} -> HTTP {}", url, resp.statusCode());
                    continue;
                }

                String body = resp.body();
                JsonNode root = om.readTree(body);

                // Single object with id
                if (root.isObject()) {
                    // { "id": 123, "title":"..." } or { "book": { "id":... } }
                    JsonNode idNode = root.path("id");
                    if (idNode.isNumber()) return idNode.asLong();
                    JsonNode bookNode = root.path("book");
                    if (bookNode.isObject() && bookNode.has("id")) return bookNode.get("id").asLong();
                }

                // Array of books
                if (root.isArray()) {
                    Long found = null;
                    int matches = 0;
                    for (JsonNode n : root) {
                        String t = n.path("title").asText(null);
                        if (t != null && t.equalsIgnoreCase(title)) {
                            JsonNode idNode = n.get("id");
                            if (idNode != null && idNode.isNumber()) {
                                found = idNode.asLong();
                                matches++;
                            }
                        }
                    }
                    if (matches == 1) return found;
                    if (matches > 1) {
                        throw new IllegalArgumentException("Book title '" + title + "' is ambiguous (" + matches + " matches).");
                    }
                    // else: no match in this array; try next path
                }
            } catch (Exception e) {
                log.warn("Title resolve failed for {} ({}): {}", url, e.getClass().getSimpleName(), e.getMessage());
            }
        }

        log.warn("Could not resolve title '{}' using base '{}'", title, base);
        return null;
    }

    // -------- Small DTOs --------
    public static class LendingDTO {
        private Long id;
        private Long bookId;
        private String readerEmail;
        private LocalDate startDate;
        private LocalDate dueDate;
        private LocalDate returnedDate;
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getBookId() { return bookId; }
        public void setBookId(Long bookId) { this.bookId = bookId; }
        public String getReaderEmail() { return readerEmail; }
        public void setReaderEmail(String readerEmail) { this.readerEmail = readerEmail; }
        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        public LocalDate getDueDate() { return dueDate; }
        public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
        public LocalDate getReturnedDate() { return returnedDate; }
        public void setReturnedDate(LocalDate returnedDate) { this.returnedDate = returnedDate; }
    }

    public static class RecommendationSummaryDTO {
        private Long bookId;
        private long positives;
        private long negatives;
        private long total;
        private double positiveRatio;
        public Long getBookId() { return bookId; }
        public void setBookId(Long bookId) { this.bookId = bookId; }
        public long getPositives() { return positives; }
        public void setPositives(long positives) { this.positives = positives; }
        public long getNegatives() { return negatives; }
        public void setNegatives(long negatives) { this.negatives = negatives; }
        public long getTotal() { return total; }
        public void setTotal(long total) { this.total = total; }
        public double getPositiveRatio() { return positiveRatio; }
        public void setPositiveRatio(double positiveRatio) { this.positiveRatio = positiveRatio; }
    }

    public static class LendingEvent {
        private Long lendingId;
        private Long bookId;
        private String readerEmail;
        private String type;
        public LendingEvent() {}
        public LendingEvent(Long lendingId, Long bookId, String readerEmail, String type) {
            this.lendingId = lendingId; this.bookId = bookId; this.readerEmail = readerEmail; this.type = type;
        }
        public Long getLendingId() { return lendingId; }
        public void setLendingId(Long lendingId) { this.lendingId = lendingId; }
        public Long getBookId() { return bookId; }
        public void setBookId(Long bookId) { this.bookId = bookId; }
        public String getReaderEmail() { return readerEmail; }
        public void setReaderEmail(String readerEmail) { this.readerEmail = readerEmail; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }
}

