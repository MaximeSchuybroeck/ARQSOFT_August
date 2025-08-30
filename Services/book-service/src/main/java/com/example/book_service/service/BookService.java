package com.example.book_service.service;

import com.example.book_service.repository.BookRepository;
import com.example.book_service.dto.BookDTO;
import com.example.book_service.entity.Book;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookService {
    @Autowired
    private BookRepository repo;

    public BookDTO add(BookDTO dto) {
        // Check if a book with the same title already exists
        if (repo.existsByTitle(dto.getTitle())) {
            throw new IllegalArgumentException("A book with the title '" + dto.getTitle() + "' already exists.");
        }

        Book b = new Book();
        b.setTitle(dto.getTitle());
        b.setGenre(dto.getGenre());
        b.setCoverUrl(dto.getCoverUrl());
        b.setAuthorId(dto.getAuthorId());

        return toDTO(repo.save(b));
    }


    public List<BookDTO> searchByTitle(String title) {
        return repo.findByTitleContainingIgnoreCase(title)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<BookDTO> getTopBooks() {
        return repo.findTop5ByBorrowed()
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<String> getTopGenres() {
        return repo.findTop5Genres();
    }
    public List<BookDTO> getAllBooks() {
        return repo.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public void deleteBook(Long id) {
        if (!repo.existsById(id)) {
            throw new EntityNotFoundException("Book not found with ID: " + id);
        }
        repo.deleteById(id);
    }

    public boolean existsById(Long id) {
        return repo.existsById(id);
    }


    private BookDTO toDTO(Book b) {
        return new BookDTO(b.getId(), b.getTitle(), b.getGenre(), b.getCoverUrl(), b.getAuthorId());
    }

}