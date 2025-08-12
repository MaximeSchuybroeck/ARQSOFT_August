package com.example.reader_service.dto;

import com.example.reader_service.entity.AcquisitionSuggestion.Status;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class AcqSugResponseDTO {
    private long id;

    private String title;
    private String genre;
    private String authorFirstName;
    private String authorLastName;
    private int timesSuggested;

    private Status status;
}
