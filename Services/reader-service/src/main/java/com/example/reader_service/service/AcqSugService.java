package com.example.reader_service.service;

import com.example.reader_service.dto.AcqSugRequestDTO;
import com.example.reader_service.dto.AcqSugResponseDTO;
import com.example.reader_service.entity.AcquisitionSuggestion;
import com.example.reader_service.entity.AcquisitionSuggestion.Status;
import com.example.reader_service.repository.AcqSugRepository;
import com.example.reader_service.repository.ReaderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AcqSugService {

    private final AcqSugRepository repository;

    public AcqSugResponseDTO create(AcqSugRequestDTO req) {

        // Check if a suggestion for the same title already exists
        AcquisitionSuggestion existing = repository.findByTitle(req.getTitle());

        if (existing != null) {
            // Increment timesSuggested and update the timestamp
            existing.setTimesSuggested(existing.getTimesSuggested() + 1);
            existing = repository.save(existing);
            return toDTO(existing);
        }

        // The suggestion does not exist so we create a new one
        AcquisitionSuggestion entity = new AcquisitionSuggestion();
        entity.setTitle(req.getTitle());
        entity.setGenre(req.getGenre());
        entity.setTimesSuggested(1);
        entity.setAuthorFirstName(req.getAuthorFirstName());
        entity.setAuthorLastName(req.getAuthorLastName());

        entity = repository.save(entity);
        return toDTO(entity);
    }


    // librarian overview
    public List<AcqSugResponseDTO> listByStatus(Status status) {
        return repository.findByStatus(status).stream().map(this::toDTO).toList();
    }

    public AcqSugResponseDTO findByTitle(String title) {
        return toDTO(repository.findByTitle(title));
    }

    public List<AcqSugResponseDTO> getAllSuggestions() {
        return repository.findAll().stream()
                .sorted(Comparator.comparing(AcquisitionSuggestion::getId)) // sort by ID ascending
                .map(this::toDTO)
                .collect(Collectors.toList());
    }


    private AcqSugResponseDTO toDTO (AcquisitionSuggestion s) {
        return AcqSugResponseDTO.builder()
                .id(s.getId())
                .title(s.getTitle())
                .authorFirstName(s.getAuthorFirstName())
                .authorLastName(s.getAuthorLastName())
                .genre(s.getGenre())
                .timesSuggested(s.getTimesSuggested())
                .status(s.getStatus())
                .build();
    }


    public List<AcqSugResponseDTO> getTop5Suggestions() {
        return repository.findAll().stream()
                .filter(suggestion -> suggestion.getStatus() != AcquisitionSuggestion.Status.APPROVED) // keep only not APPROVED
                .sorted(Comparator.comparingInt(AcquisitionSuggestion::getTimesSuggested).reversed()) // sort by timesSuggested desc
                .limit(5) // top 5
                .map(this::toDTO)
                .collect(Collectors.toList());
    }



}
