package com.example.lending_service.repository;

import com.example.lending_service.entity.Lending;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LendingRepository extends JpaRepository<Lending, Long> {

    @Query("SELECT l FROM Lending l WHERE l.returnedDate IS NULL AND l.dueDate < CURRENT_DATE ORDER BY l.dueDate ASC")
    List<Lending> findOverdueLendings();

    @Query(
            value = """
         SELECT AVG((returned_date - start_date)) 
         FROM lending_service.lending 
         WHERE returned_date IS NOT NULL
         """,
            nativeQuery = true
    )
    Double averageLendingDuration();
    Optional<Lending> findByBookIdAndReturnedDateIsNull(Long bookId);
    List<Lending> findAll();



}