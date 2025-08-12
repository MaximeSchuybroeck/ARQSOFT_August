package com.example.reader_service.controller;

import com.example.reader_service.dto.ReaderDTO;
import com.example.reader_service.repository.ReaderRepository;
import com.example.reader_service.service.ReaderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/readers")
public class ReaderController {

    @Autowired
    private ReaderService service;
    @Autowired
    private ReaderRepository repository;

    @PostMapping
    public Object register(@RequestBody ReaderDTO dto) {
        return service.registerReader(dto);
    }

    @GetMapping("/email")
    public ResponseEntity<?> getByEmail(@RequestParam String email) {
        ReaderDTO dto = service.getByEmail(email);
        if (dto == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Reader with email '" + email + "' not found");
        }
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/phone")
    public ResponseEntity<?> getByPhone(@RequestParam String phone) {
        ReaderDTO dto = service.getByPhone(phone);
        if (dto == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Reader with phone '" + phone + "' not found");
        }
        return ResponseEntity.ok(dto);
    }
    @GetMapping("/{email}/quote")
    public String getFunnyQuote(@PathVariable String email) {
        return service.getFunnyQuote(email);
    }

    @GetMapping("/{email}/genres")
    public List<String> getPreferredGenres(@PathVariable String email) {
        return service.getPreferredGenres(email);
    }

    @GetMapping("/email/exist")
    public ResponseEntity<Boolean> checkReaderExists(@RequestParam String value) {
        boolean exists = repository.existsByEmail(value);
        return ResponseEntity.ok(exists);
    }
}