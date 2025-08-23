package com.example.lending_service.repository;

import com.example.lending_service.entity.Recommendation;
import com.example.lending_service.entity.Recommendation.Sentiment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {

    boolean existsByLendingId(Long lendingId);

    long countByBookIdAndSentiment(Long bookId, Sentiment sentiment);

    @Query("""
        SELECT COUNT(r) FROM Recommendation r WHERE r.bookId = :bookId
    """)
    long countByBookId(Long bookId);
}
