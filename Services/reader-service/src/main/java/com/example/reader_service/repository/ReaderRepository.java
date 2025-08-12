package com.example.reader_service.repository;

import com.example.reader_service.entity.Reader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for Reader.
 */
@Repository
public interface ReaderRepository extends JpaRepository<Reader, String> {

    /**
     * Finds a reader by email (primary key).
     *
     * @param email the email to search for
     * @return Optional containing the Reader if found
     */
    @Query("SELECT r FROM Reader r WHERE r.email = :email")
    Optional<Reader> findByEmail(@Param("email") String email);

    /**
     * Finds a reader by phone number.
     *
     * @param phone the phone number to search for
     * @return Optional containing the Reader if found
     */
    @Query("SELECT r FROM Reader r WHERE r.phone = :phone")
    Optional<Reader> findByPhone(@Param("phone") String phone);

    /**
     * Checks whether a reader exists with the given email.
     *
     * @param email the email to check
     * @return true if a reader exists with the email, otherwise false
     */
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Reader r WHERE r.email = :email")
    boolean existsByEmail(@Param("email") String email);
}
