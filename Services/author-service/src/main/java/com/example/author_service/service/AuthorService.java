package com.example.author_service.service;

import com.example.author_service.Entity.Author;
import com.example.author_service.dto.AuthorDTO;
import com.example.author_service.repository.AuthorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthorService {
    @Autowired
    private AuthorRepository repository;

    public AuthorDTO createAuthor(AuthorDTO dto) {
        Author author = new Author();
        author.setFirstName(dto.getFirstName());
        author.setLastName(dto.getLastName());
        author.setPhotoUrl(dto.getPhotoUrl());
        return toDTO(repository.save(author));
    }

    public List<AuthorDTO> getAllAuthors() {
        return repository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }


    private AuthorDTO toDTO(Author a) {
        return new AuthorDTO(a.getId(), a.getFirstName(), a.getLastName(),a.getPhotoUrl());
    }
}