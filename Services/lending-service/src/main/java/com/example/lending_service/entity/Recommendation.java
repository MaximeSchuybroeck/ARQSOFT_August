package com.example.lending_service.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "recommendation", schema = "lending_service")
public class Recommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // We enforce one recommendation per lending
    @Column(name = "lending_id", nullable = false, unique = true)
    private Long lendingId;

    @Column(name = "book_id", nullable = false)
    private Long bookId;

    @Column(name = "reader_email", nullable = false)
    private String readerEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "sentiment", nullable = false)
    private Sentiment sentiment;

    @Column(name = "comment")
    private String comment;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Sentiment { POSITIVE, NEGATIVE }
}
