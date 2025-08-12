package com.example.reporting_service.repository;

import com.example.reporting_service.entity.AuthorBorrowStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuthorBorrowStatsRepository extends JpaRepository<AuthorBorrowStats, Long> {
    List<AuthorBorrowStats> findTop5ByOrderByBorrowCountDesc();
}