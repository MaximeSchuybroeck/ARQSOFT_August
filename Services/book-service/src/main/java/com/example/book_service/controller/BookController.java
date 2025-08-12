package com.example.book_service.controller;

import com.example.book_service.dto.BookDTO;
import com.example.book_service.repository.BookRepository;
import com.example.book_service.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {

    @Autowired
    private BookService service;
    private BookRepository repository;

    @PostMapping
    public ResponseEntity<BookDTO> addBook(@RequestBody BookDTO dto) {
        BookDTO savedBook = service.add(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedBook);
    }

    @GetMapping("/search")
    public ResponseEntity<List<BookDTO>> search(@RequestParam String title) {
        List<BookDTO> result = service.searchByTitle(title);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/top")
    public ResponseEntity<List<BookDTO>> topBooks() {
        return ResponseEntity.ok(service.getTopBooks());
    }

    @GetMapping("/top-genres")
    public ResponseEntity<List<String>> topGenres() {
        return ResponseEntity.ok(service.getTopGenres());
    }

    @GetMapping("/all")
    public ResponseEntity<List<BookDTO>> getAllBooks() {
        return ResponseEntity.ok(service.getAllBooks());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        service.deleteBook(id);
        return ResponseEntity.noContent().build(); // HTTP 204
    }
    @GetMapping("/{id}/exist")
    public ResponseEntity<Boolean> checkBookExists(@PathVariable Long id) {
        return ResponseEntity.ok(service.existsById(id));
    }



}
