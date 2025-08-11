package pt.psoft.g1.psoftg1.lendingmanagement.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Lending;
import pt.psoft.g1.psoftg1.shared.services.Page;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
@Repository

public interface LendingRepository {
    Optional<Lending> findByLendingNumber(String lendingNumber);
    List<Lending> listByReaderNumberAndIsbn(String readerNumber, String isbn);
    int getCountFromCurrentYear();
    List<Lending> listOutstandingByReaderNumber(String readerNumber);
    Double getAverageDuration();
    Double getAvgLendingDurationByIsbn(String isbn);


    List<Lending> getOverdue(Page page);
    List<Lending> searchLendings(Page page, String readerNumber, String isbn, Boolean returned, LocalDate startDate, LocalDate endDate);

    Lending save(Lending lending);

    void delete(Lending lending);

    @Query("SELECT l.book.genre.genre " +
            "FROM Lending l WHERE l.readerDetails.reader.id = :readerId " +
            "GROUP BY l.book.genre.genre " +
            "ORDER BY COUNT(l) DESC LIMIT 1")
    String findMostLentGenreByReader(@Param("readerId") Long readerId);

    void deleteAll();
}
