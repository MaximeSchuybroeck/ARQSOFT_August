package com.example.reader_service.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
@Table(name = "acquisition_suggestions", schema = "reader_service")
public class AcquisitionSuggestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "genre")
    private String genre;

    @Column(name = "author_first_name")
    private String authorFirstName;

    @Column(name = "author_last_name")
    private String authorLastName;

    @Column(name = "times_suggested", nullable = false)
    private int timesSuggested;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.NEW;


    public enum Status {
        NEW, UNDER_REVIEW, APPROVED, REJECTED
    }

    // -----------------------------
    // Getters & Setters (explicit)
    // -----------------------------

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getAuthorFirstName() {
        return authorFirstName;
    }

    public void setAuthorFirstName(String authorFirstName) {
        this.authorFirstName = authorFirstName;
    }

    public String getAuthorLastName() {
        return authorLastName;
    }

    public void setAuthorLastName(String authorLastName) {
        this.authorLastName = authorLastName;
    }

    public int getTimesSuggested() {
        return timesSuggested;
    }

    public void setTimesSuggested(int timesSuggested) {
        this.timesSuggested = timesSuggested;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

}
