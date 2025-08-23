package com.example.lending_service.controller;

import com.example.lending_service.entity.Recommendation;
import com.example.lending_service.repository.RecommendationRepository;
import com.example.lending_service.service.LendingService;
import com.example.lending_service.service.LendingService.LendingDTO;
import com.example.lending_service.service.LendingService.RecommendationSummaryDTO;
import com.example.lending_service.dto.ReturnRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lendings")
public class LendingController {

    private final LendingService service;
    private final RecommendationRepository recRepo;

    public LendingController(LendingService service, RecommendationRepository recRepo) {
        this.service = service;
        this.recRepo = recRepo;
    }

    // ---------- Borrow ----------
    @PostMapping("/borrow")
    public ResponseEntity<LendingDTO> borrow(
            @RequestParam Long bookId,
            @RequestParam String readerEmail,
            @RequestParam(required = false) Integer days
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.borrow(bookId, readerEmail, days));
    }

    // ---------- Return (by lending id) ----------
    // Example: POST /api/lendings/return/1?recommended=true&comment=Great%20read
    @PostMapping({"/return/{lendingId:\\d+}", "/{lendingId:\\d+}/return"})
    public ResponseEntity<LendingDTO> returnById(
            @PathVariable Long lendingId,
            @RequestParam(required = false) Boolean recommended,
            @RequestParam(required = false) String comment
    ) {
        return ResponseEntity.ok(service.returnById(lendingId, recommended, comment));
    }

    // ---------- Return (by reader + bookId) ----------
    // Body: { "readerEmail": "a@b.com", "bookId": 1 }
    // Optional query params: ?recommended=true&comment=Nice
    @PostMapping("/return")
    public ResponseEntity<LendingDTO> returnByReaderAndBook(
            @RequestBody ReturnRequest request,
            @RequestParam(required = false) Boolean recommended,
            @RequestParam(required = false) String comment
    ) {
        return ResponseEntity.ok(service.returnByReaderAndBook(request, recommended, comment));
    }

    // ---------- Return (by reader + book title) ----------
    // Example: POST /api/lendings/return/by-title?readerEmail=a@b.com&bookTitle=The%20Hobbit&recommended=true
    @PostMapping("/return/by-title")
    public ResponseEntity<LendingDTO> returnByReaderAndBookTitle(
            @RequestParam String readerEmail,
            @RequestParam String bookTitle,
            @RequestParam(required = false) Boolean recommended,
            @RequestParam(required = false) String comment
    ) {
        return ResponseEntity.ok(service.returnByReaderAndBookTitle(readerEmail, bookTitle, recommended, comment));
    }

    // ---------- Queries ----------
    @GetMapping
    public List<LendingDTO> getAll() {
        return service.getAllLendings();
    }

    @GetMapping("/overdue")
    public List<LendingDTO> getOverdue() {
        return service.getOverdueLendings();
    }

    @GetMapping("/average-duration")
    public Double averageDuration() {
        return service.getAverageLendingDuration();
    }

    // ---------- Recommendations ----------
    @PostMapping("/recommendations")
    public ResponseEntity<Void> createRecommendation(
            @RequestBody Recommendation recommendation,
            @RequestParam(required = false) Boolean recommended
    ) {
        if (recommendation.getSentiment() == null && recommended != null) {
            recommendation.setSentiment(Boolean.TRUE.equals(recommended)
                    ? Recommendation.Sentiment.POSITIVE
                    : Recommendation.Sentiment.NEGATIVE);
        }
        if (recommendation.getSentiment() == null) {
            return ResponseEntity.badRequest().build();
        }
        recRepo.save(recommendation);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/books/{bookId}/recommendations/summary")
    public RecommendationSummaryDTO getRecommendationSummary(@PathVariable Long bookId) {
        return service.getRecommendationSummary(bookId);
    }

    @GetMapping("/health")
    public String health() {
        return "OK";
    }

    // ---------- Error mapping for nicer messages ----------
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleAlreadyReturned(IllegalStateException ex) {
        // e.g. trying to return an already returned lending, or duplicate recommendation
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleBadRequest(IllegalArgumentException ex) {
        // e.g. no active lending found, bad inputs, unknown lending id
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}
