package com.example.lending_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RecommendationResponse {
    private Long lendingId;
    private Long bookId;
    private String readerEmail;
    private String sentiment; // "POSITIVE" | "NEGATIVE"
    private String comment;
}
