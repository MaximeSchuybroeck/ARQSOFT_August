package com.example.lending_service.dto;

import com.example.lending_service.entity.Lending;
import lombok.Data;

import java.time.LocalDate;
@Data
public class LendingDTO {
    private Long id;
    private String readerEmail;
    private Long bookId;
    private LocalDate startDate;
    private LocalDate dueDate;
    private LocalDate returnedDate;

    public LendingDTO() {}

    public LendingDTO(Lending lending) {
        this.id = lending.getId();
        this.readerEmail = lending.getReaderEmail();
        this.bookId = lending.getBookId();
        this.startDate = lending.getStartDate();
        this.dueDate = lending.getDueDate();
    }


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