package com.example.lending_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Data
@Table(name = "lending",schema = "lending_service")
public class Lending {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    @Id
    private Long id;
    @Column(name = "reader_email")
    private String readerEmail;
    @Column(name = "book_id")
    private Long bookId;
    @Column(name = "start_date")
    private LocalDate startDate;
    @Column(name = "due_date")
    private LocalDate dueDate;
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

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getReturnedDate() {
        return returnedDate;
    }

    public void setReturnedDate(LocalDate returnedDate) {
        this.returnedDate = returnedDate;
    }
}