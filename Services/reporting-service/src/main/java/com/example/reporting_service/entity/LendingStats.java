package com.example.reporting_service.entity;

import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
@Table(name = "lending_stats", schema = "reporting_service")
public class LendingStats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "readerEmail")
    private String readerEmail;
    @Column(name = "genre")
    private String genre;
    @Column(name = "book_id")
    private Long bookId;
    @Column(name = "start_date")
    private LocalDate startDate;
    @Column(name = "returned_date")
    private LocalDate returnedDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReaderEmail() {
        return readerEmail;
    }

    public void setReaderEmail(String readerEmail) {
        this.readerEmail = readerEmail;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getReturnedDate() {
        return returnedDate;
    }

    public void setReturnedDate(LocalDate returnedDate) {
        this.returnedDate = returnedDate;
    }
}