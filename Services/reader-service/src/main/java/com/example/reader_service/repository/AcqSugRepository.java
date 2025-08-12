package com.example.reader_service.repository;

import com.example.reader_service.entity.AcquisitionSuggestion;
import com.example.reader_service.entity.AcquisitionSuggestion.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AcqSugRepository extends JpaRepository<AcquisitionSuggestion, Long> {

    /**
     * Finds a single suggestion by its exact title.
     *
     * @param title exact title to search
     * @return matching AcquisitionSuggestion or null
     */
    @Query("SELECT a FROM AcquisitionSuggestion a WHERE a.title = :title")
    AcquisitionSuggestion findByTitle(@Param("title") String title);

    /**
     * Checks if a suggestion exists for the given exact title.
     *
     * @param title exact title to check
     * @return true if at least one suggestion exists, false otherwise
     */
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM AcquisitionSuggestion a WHERE a.title = :title")
    boolean existsByTitle(@Param("title") String title);

    /**
     * Finds all suggestions with the given status.
     *
     * @param status status to filter by
     * @return list of suggestions with the given status
     */
    @Query("SELECT a FROM AcquisitionSuggestion a WHERE a.status = :status")
    List<AcquisitionSuggestion> findByStatus(@Param("status") Status status);



}
