package pt.psoft.g1.psoftg1.bookmanagement.Services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.data.domain.PageRequest;
import pt.psoft.g1.psoftg1.bookmanagement.services.BookServiceImpl;
import pt.psoft.g1.psoftg1.bookmanagement.services.TopByGenreDTO;
import pt.psoft.g1.psoftg1.lendingmanagement.repositories.LendingRepository; // adjust
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class TopXWithinTopYServiceTest {

    @Mock pt.psoft.g1.psoftg1.lendingmanagement.repositories.LendingRepository lendingRepository;

    @InjectMocks pt.psoft.g1.psoftg1.bookmanagement.services.BookServiceImpl service;

    @Test
    void returnsTopXBooksForEachOfTopYGenres() {
        int topX = 2, topY = 2;
        java.time.LocalDate since = java.time.LocalDate.now().minusYears(1);

        // ---- Top genres page (List<Object[]>)
        java.util.List<Object[]> genresRows = new java.util.ArrayList<>();
        genresRows.add(new Object[] { "fantasy", 10L });
        genresRows.add(new Object[] { "sci-fi", 8L });

        org.springframework.data.domain.Page<Object[]> genresPage =
                new org.springframework.data.domain.PageImpl<>(
                        genresRows,
                        org.springframework.data.domain.PageRequest.of(0, topY),
                        genresRows.size()
                );
        org.mockito.Mockito.when(
                lendingRepository.findTopGenresByLendCountSince(
                        org.mockito.ArgumentMatchers.eq(since),
                        org.mockito.ArgumentMatchers.any()
                )
        ).thenReturn(genresPage);

        // ---- Declare Book mocks (so f1/f2/s1 exist)
        pt.psoft.g1.psoftg1.bookmanagement.model.Book f1 =
                org.mockito.Mockito.mock(pt.psoft.g1.psoftg1.bookmanagement.model.Book.class);
        pt.psoft.g1.psoftg1.bookmanagement.model.Book f2 =
                org.mockito.Mockito.mock(pt.psoft.g1.psoftg1.bookmanagement.model.Book.class);
        pt.psoft.g1.psoftg1.bookmanagement.model.Book s1 =
                org.mockito.Mockito.mock(pt.psoft.g1.psoftg1.bookmanagement.model.Book.class);

        // ---- Fantasy rows (List<Object[]>)
        java.util.List<Object[]> fantasyRows = new java.util.ArrayList<>();
        fantasyRows.add(new Object[] { f1, 7L });
        fantasyRows.add(new Object[] { f2, 5L });

        org.springframework.data.domain.Page<Object[]> fantasyPage =
                new org.springframework.data.domain.PageImpl<>(
                        fantasyRows,
                        org.springframework.data.domain.PageRequest.of(0, topX),
                        fantasyRows.size()
                );
        org.mockito.Mockito.when(
                lendingRepository.findTopBooksByGenreSince(
                        org.mockito.ArgumentMatchers.eq("fantasy"),
                        org.mockito.ArgumentMatchers.eq(since),
                        org.mockito.ArgumentMatchers.any()
                )
        ).thenReturn(fantasyPage);

        // ---- Sci-fi rows (List<Object[]>)
        java.util.List<Object[]> scifiRows = new java.util.ArrayList<>();
        scifiRows.add(new Object[] { s1, 6L });

        org.springframework.data.domain.Page<Object[]> scifiPage =
                new org.springframework.data.domain.PageImpl<>(
                        scifiRows,
                        org.springframework.data.domain.PageRequest.of(0, topX),
                        scifiRows.size()
                );
        org.mockito.Mockito.when(
                lendingRepository.findTopBooksByGenreSince(
                        org.mockito.ArgumentMatchers.eq("sci-fi"),
                        org.mockito.ArgumentMatchers.eq(since),
                        org.mockito.ArgumentMatchers.any()
                )
        ).thenReturn(scifiPage);

        // ---- Act
        java.util.List<pt.psoft.g1.psoftg1.bookmanagement.services.TopByGenreDTO> result =
                service.findTopXWithinTopYGenres(topX, topY);

        // ---- Assert (no getters; check identity/order)
        org.assertj.core.api.Assertions.assertThat(result).hasSize(2);
        org.assertj.core.api.Assertions.assertThat(result.get(0).genre()).isEqualTo("fantasy");
        org.assertj.core.api.Assertions.assertThat(result.get(0).books()).containsExactly(f1, f2);
        org.assertj.core.api.Assertions.assertThat(result.get(1).genre()).isEqualTo("sci-fi");
        org.assertj.core.api.Assertions.assertThat(result.get(1).books()).containsExactly(s1);
    }
}
