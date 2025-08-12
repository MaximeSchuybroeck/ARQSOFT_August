package com.example.book_service.dto;

import lombok.Data;

@Data
public class BookDTO {
    private Long id;
    private String title;
    private String genre;
    private String coverUrl;
    private Long authorId;

    public BookDTO(Long id, String title, String genre, String coverUrl, Long authorId) {
        this.id = id;
        this.title = title;
        this.genre = genre;
        this.coverUrl = coverUrl;
        this.authorId = authorId;
    }

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

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }
}
