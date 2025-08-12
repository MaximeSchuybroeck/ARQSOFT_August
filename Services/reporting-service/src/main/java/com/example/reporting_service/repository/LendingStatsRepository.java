package com.example.reporting_service.repository;

import com.example.reporting_service.entity.LendingStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LendingStatsRepository extends JpaRepository<LendingStats, Long> {

    @Query("SELECT s.genre, COUNT(s.id) FROM LendingStats s GROUP BY s.genre ORDER BY COUNT(s.id) DESC")
    List<Object[]> topGenres();

    @Query("SELECT s.readerEmail, COUNT(s.id) FROM LendingStats s WHERE s.genre = ?1 GROUP BY s.readerEmail ORDER BY COUNT(s.id) DESC LIMIT 5")
    List<Object[]> topReadersByGenre(String genre);
}