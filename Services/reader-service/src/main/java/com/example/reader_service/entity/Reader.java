package com.example.reader_service.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * JPA entity representing a library reader (MySQL-backed).
 */
@Entity
@Data
@Table(name = "readers", schema = "reader_service",
        indexes = {
                @Index(name = "idx_reader_phone", columnList = "phone")
        })
public class Reader {

    /**
     * We use the email as the natural primary key.
     */
    @Id
    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "name")
    private String name;

    @Column(name = "phone")
    private String phone;

    @Column(name = "photo_url")
    private String photoUrl;

    /**
     * Preferred genres stored in a separate collection table.
     */
    @ElementCollection
    @CollectionTable(
            name = "reader_preferred_genres",
            schema = "reader_service",
            joinColumns = @JoinColumn(name = "reader_email", referencedColumnName = "email")
    )
    @Column(name = "genre")
    private List<String> preferredGenres;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    // -----------------------------
    // Explicit getters & setters
    // -----------------------------

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public List<String> getPreferredGenres() {
        return preferredGenres;
    }

    public void setPreferredGenres(List<String> preferredGenres) {
        this.preferredGenres = preferredGenres;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }
}
