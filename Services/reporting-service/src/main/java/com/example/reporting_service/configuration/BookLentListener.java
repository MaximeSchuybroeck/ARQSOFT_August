package com.example.reporting_service.configuration;

import com.example.reporting_service.dto.BookLentEvent;
import com.example.reporting_service.entity.AuthorBorrowStats;
import com.example.reporting_service.entity.LendingStats;
import com.example.reporting_service.repository.AuthorBorrowStatsRepository;
import com.example.reporting_service.repository.LendingStatsRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class BookLentListener {

    private final LendingStatsRepository lendingStatsRepo;
    private final AuthorBorrowStatsRepository authorStatsRepo;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public BookLentListener(LendingStatsRepository lendingStatsRepo, AuthorBorrowStatsRepository authorStatsRepo) {
        this.lendingStatsRepo = lendingStatsRepo;
        this.authorStatsRepo = authorStatsRepo;
    }

    @RabbitListener(queues = "${reporting.queue}")
    public void handleBookLentEvent(String message) {
        try {
            BookLentEvent event = objectMapper.readValue(message, BookLentEvent.class);

            // Save to LendingStats (existing logic)
            LendingStats stats = new LendingStats();
            stats.setReaderEmail(event.getReaderEmail());
            stats.setBookId(event.getBookId());
            stats.setGenre(event.getGenre());
            stats.setStartDate(event.getStartDate());
            lendingStatsRepo.save(stats);

            // Update Author Borrow Stats
            AuthorBorrowStats authorStats = authorStatsRepo.findById(event.getAuthorId())
                    .orElseGet(() -> {
                        AuthorBorrowStats newStats = new AuthorBorrowStats();
                        newStats.setAuthorId(event.getAuthorId());
                        newStats.setAuthorName(event.getAuthorName());
                        newStats.setBorrowCount(0L);
                        return newStats;
                    });

            authorStats.setBorrowCount(authorStats.getBorrowCount() + 1);
            authorStatsRepo.save(authorStats);

        } catch (Exception e) {
            e.printStackTrace(); // Consider proper logging
        }
    }
}

