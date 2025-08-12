package com.example.reader_service.controller;

import com.example.reader_service.dto.AcqSugRequestDTO;
import com.example.reader_service.dto.AcqSugResponseDTO;
import com.example.reader_service.entity.AcquisitionSuggestion;
import com.example.reader_service.entity.AcquisitionSuggestion.Status;
import com.example.reader_service.service.AcqSugService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/suggestions")
public class AcqSugController {

    private final AcqSugService service;

    /**
     * Creates a new acquisition suggestion.
     *
     * POST /api/suggestions
     *
     * @param request payload containing title, author names, genre, etc.
     * @return 201 Created with the created suggestion as DTO
     */
    @PostMapping
    public ResponseEntity<AcqSugResponseDTO> suggest(@Valid @RequestBody AcqSugRequestDTO request) {
        AcqSugResponseDTO created = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Retrieves all acquisition suggestions that have been approved by the librarian.
     *
     * GET /api/suggestions/approved
     *
     * @return 200 OK with a list of suggestions that have status APPROVED
     */
    @GetMapping("/approved")
    public ResponseEntity<List<AcqSugResponseDTO>> getApprovedSuggestions() {
        List<AcqSugResponseDTO> approved = service.listByStatus(Status.APPROVED);
        return ResponseEntity.ok(approved);
    }

    /**
     * Retrieves acquisition suggestions filtered by status.
     * If the status is not provided, NEW is used as the default.
     *
     * GET /api/suggestions/{status}
     *
     * @param status optional status filter (NEW by default)
     * @return 200 OK with a list of suggestions matching the given status
     */
    @GetMapping("/statusList/{status}")
    public ResponseEntity<List<AcqSugResponseDTO>> listByStatus(@PathVariable(required = false) Status status) {
        List<AcqSugResponseDTO> list = (status == null)
                ? service.listByStatus(Status.NEW)
                : service.listByStatus(status);
        return ResponseEntity.ok(list);
    }

    /**
     * Retrieves all acquisition suggestions regardless of status.
     *
     * GET /api/suggestions/all
     *
     * @return 200 OK with a list of all suggestions
     */
    @GetMapping("/all")
    public ResponseEntity<List<AcqSugResponseDTO>> getAllSuggestions() {
        return ResponseEntity.ok(service.getAllSuggestions());
    }

    /**
     * Retrieves a single acquisition suggestion by its exact title.
     *
     * GET /api/suggestions/{title}
     *
     * @param title the exact title to search for
     * @return 200 OK with the matching suggestion as a DTO, or 404 if not found
     */
    @GetMapping("/{title}")
    public ResponseEntity<AcqSugResponseDTO> getSuggestionByTitle(@PathVariable String title) {
        AcqSugResponseDTO suggestion = service.findByTitle(title);
        if (suggestion == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Created a new acquisition with title: " + title);
        }
        return ResponseEntity.ok(suggestion);
    }

    /**
     * Retrieves the top 5 acquisition suggestions with the highest timesSuggested
     * where the status is NOT APPROVED.
     *
     * GET /api/suggestions/top5Suggestions
     *
     * @return 200 OK with a list of the top 5 most suggested books (not approved)
     */
    @GetMapping("/top5Suggestions")
    public ResponseEntity<List<AcqSugResponseDTO>> getTop5Suggestions() {
        return ResponseEntity.ok(service.getTop5Suggestions());
    }


}
