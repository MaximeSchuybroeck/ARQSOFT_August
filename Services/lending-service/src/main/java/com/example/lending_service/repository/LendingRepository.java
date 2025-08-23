package com.example.lending_service.repository;

import com.example.lending_service.entity.Lending;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;   // <-- this one
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LendingRepository extends JpaRepository<Lending, Long> {

    @Query("SELECT l FROM Lending l WHERE l.returnedDate IS NULL AND l.dueDate < CURRENT_DATE ORDER BY l.dueDate ASC")
    List<Lending> findOverdueLendings();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Lending> findByIdAndReturnedDateIsNull(Long id);

    Optional<Lending> findByBookIdAndReaderEmailAndReturnedDateIsNull(Long bookId, String readerEmail);
    Optional<Lending> findByBookIdAndReturnedDateIsNull(Long bookId);

    @Query(
            value =
                    "SELECT AVG((returned_date - start_date)::float8) " +
                            "FROM lending_service.lending " +
                            "WHERE returned_date IS NOT NULL",
            nativeQuery = true
    )
    Double averageLendingDuration();
}
