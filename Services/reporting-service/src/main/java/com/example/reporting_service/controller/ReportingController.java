package com.example.reporting_service.controller;

import com.example.reporting_service.entity.AuthorBorrowStats;
import com.example.reporting_service.repository.AuthorBorrowStatsRepository;
import com.example.reporting_service.repository.LendingStatsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportingController {

    private final LendingStatsRepository repo;

    public ReportingController(LendingStatsRepository repo) {
        this.repo = repo;
    }
    @Autowired
    private AuthorBorrowStatsRepository authorBorrowStatsRepository;

    @GetMapping("/top-genres")
    public List<Object[]> getTopGenres() {
        return repo.topGenres();
    }

    @GetMapping("/top-readers/{genre}")
    public List<Object[]> getTopReadersByGenre(@PathVariable String genre) {
        return repo.topReadersByGenre(genre);
    }

    @GetMapping("/top5")
    public List<AuthorBorrowStats> getTop5Authors() {
        return authorBorrowStatsRepository.findTop5ByOrderByBorrowCountDesc();
    }
}