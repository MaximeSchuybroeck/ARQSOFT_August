package com.example.author_service.controller;

import com.example.author_service.dto.AuthorDTO;
import com.example.author_service.service.AuthorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/authors")
public class AuthorController {

    @Autowired
    private AuthorService authorService;

    @PostMapping
    public AuthorDTO createAuthor(@RequestBody AuthorDTO dto) {
        return authorService.createAuthor(dto);
    }

    @GetMapping
    public List<AuthorDTO> getAllAuthors() {
        return authorService.getAllAuthors();
    }

}
