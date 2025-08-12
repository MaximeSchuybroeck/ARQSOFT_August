package com.example.reporting_service.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "author_borrow_stats", schema = "reporting_service")
public class AuthorBorrowStats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long authorId;

    private String authorName;

    @Column(name = "borrow_count")
    private Long borrowCount = 0L;

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public Long getBorrowCount() {
        return borrowCount;
    }

    public void setBorrowCount(Long borrowCount) {
        this.borrowCount = borrowCount;
    }
}
