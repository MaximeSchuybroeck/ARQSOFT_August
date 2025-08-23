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

    private Long resolveBookIdByTitle(String title) {
        try {
            java.net.URI uri = new java.net.URI(
                    bookServiceBaseUrl + "/api/books/by-title?title=" +
                            java.net.URLEncoder.encode(title, java.nio.charset.StandardCharsets.UTF_8)
            );
            java.net.http.HttpClient http = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest req = java.net.http.HttpRequest.newBuilder(uri).GET().build();
            java.net.http.HttpResponse<String> resp = http.send(req, java.net.http.HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                String body = resp.body();
                int idx = body.indexOf("\"id\"");
                if (idx >= 0) {
                    int colon = body.indexOf(':', idx);
                    if (colon > 0) {
                        int end = colon + 1;
                        while (end < body.length() && Character.isWhitespace(body.charAt(end))) end++;
                        StringBuilder num = new StringBuilder();
                        while (end < body.length() && Character.isDigit(body.charAt(end))) {
                            num.append(body.charAt(end++));
                        }
                        if (!num.isEmpty()) {
                            return Long.parseLong(num.toString());
                        }
                    }
                }
            }
        } catch (Exception ignored) { }
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

