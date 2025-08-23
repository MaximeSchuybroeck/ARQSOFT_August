package com.example.lending_service.dto;

import lombok.Data;

@Data
public class RecommendationRequest {
    private boolean positive;
    private String comment; // optional
}
