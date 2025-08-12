package com.example.reader_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AcqSugRequestDTO {
    @NotBlank
    private String title;
    private String genre;
    private String authorFirstName;
    private String authorLastName;
}
