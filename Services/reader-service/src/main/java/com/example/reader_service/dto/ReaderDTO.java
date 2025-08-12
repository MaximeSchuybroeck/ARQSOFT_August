package com.example.reader_service.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;
@Data
public class ReaderDTO {
    private String name;
    private String email;
    private String phone;
    private String photoUrl;
    private List<String> preferredGenres;
    private LocalDate birthDate;



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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