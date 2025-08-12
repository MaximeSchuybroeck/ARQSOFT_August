package com.example.reader_service.service;

import com.example.reader_service.dto.ReaderDTO;
import com.example.reader_service.entity.Reader;
import com.example.reader_service.repository.ReaderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.MonthDay;
import java.util.List;
import java.util.Map;

@Service
public class ReaderService {

    @Autowired
    private ReaderRepository repo;

    public Object registerReader(ReaderDTO dto) {
        if (repo.findByEmail(dto.getEmail()).isPresent()) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", "Reader with this email already exists"));
        }

        Reader r = new Reader();
        r.setEmail(dto.getEmail());
        r.setName(dto.getName());
        r.setPhone(dto.getPhone());
        r.setPhotoUrl(dto.getPhotoUrl());
        r.setPreferredGenres(dto.getPreferredGenres());
        r.setBirthDate(dto.getBirthDate());

        return toDTO(repo.save(r));
    }


    public ReaderDTO getByEmail(String email) {
        return repo.findById(email).map(this::toDTO).orElse(null);
    }

    public ReaderDTO getByPhone(String phone) {
        return repo.findByPhone(phone).map(this::toDTO).orElse(null);
    }

    public String getFunnyQuote(String email) {
        Reader r = repo.findById(email).orElseThrow();
        int index = MonthDay.from(r.getBirthDate()).getDayOfMonth() % quotes.length;
        return quotes[index];
    }

    public List<String> getPreferredGenres(String email) {
        return repo.findById(email)
                .map(Reader::getPreferredGenres)
                .orElse(List.of());
    }

    private final String[] quotes = {
            "You're overdue for a great book!",
            "You read more than Google!",
            "Your brain is a book buffet!",
            "Reading is your cardio!",
            "Librarians love you!"
    };

    private ReaderDTO toDTO(Reader r) {
        ReaderDTO dto = new ReaderDTO();
        dto.setEmail(r.getEmail());
        dto.setName(r.getName());
        dto.setPhone(r.getPhone());
        dto.setPhotoUrl(r.getPhotoUrl());
        dto.setPreferredGenres(r.getPreferredGenres());
        dto.setBirthDate(r.getBirthDate());
        return dto;
    }
}
