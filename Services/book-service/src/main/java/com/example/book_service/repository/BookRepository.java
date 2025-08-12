package com.example.book_service.repository;

import com.example.book_service.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    List<Book> findByTitleContainingIgnoreCase(String title);

    @Query(value = """
    SELECT b.* 
    FROM book_service.books b
    JOIN lending_service.lending l ON b.id = l.book_id
    GROUP BY b.id
    ORDER BY COUNT(l.id) DESC
    LIMIT 5
""", nativeQuery = true)    List<Book> findTop5ByBorrowed();

    @Query(value = """
    SELECT b.genre
    FROM book_service.books b
    JOIN lending_service.lending l ON b.id = l.book_id
    GROUP BY b.genre
    ORDER BY COUNT(l.id) DESC
    LIMIT 5
""", nativeQuery = true)
    List<String> findTop5Genres();
    boolean existsByTitle(String title);
}