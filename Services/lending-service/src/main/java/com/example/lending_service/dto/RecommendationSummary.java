package com.example.lending_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RecommendationSummary {
    private Long bookId;
    private long positives;
    private long negatives;
    private long total;
    private double positiveRatio; // positives / total, 0 if total=0
}
