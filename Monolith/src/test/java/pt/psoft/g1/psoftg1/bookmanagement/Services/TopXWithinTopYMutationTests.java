package pt.psoft.g1.psoftg1.bookmanagement.Services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.bookmanagement.services.BookServiceImpl;
import pt.psoft.g1.psoftg1.lendingmanagement.repositories.LendingRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TopXWithinTopYMutationTests {

    @Mock
    LendingRepository repo;

    @InjectMocks
    BookServiceImpl service;

    @Test
    void enforcesOrdering_descByCount_perGenre() {
        LocalDate since = LocalDate.now().minusYears(1);

        // ---- Top-genre page (explicit List<Object[]> + explicit PageImpl<Object[]>)
        List<Object[]> genreRows = new ArrayList<>();
        genreRows.add(new Object[] { "fantasy", 99L });
        Page<Object[]> genres = new PageImpl<Object[]>(
                genreRows,
                PageRequest.of(0, 1),
                genreRows.size()
        );
        when(repo.findTopGenresByLendCountSince(eq(since), any(Pageable.class)))
                .thenReturn(genres);

        // ---- Books within the genre (order must be descending by count)
        Book b1 = Mockito.mock(Book.class);
        Book b2 = Mockito.mock(Book.class);

        // counts: b2=20, b1=10 → expect [b2, b1]
        List<Object[]> rows = new ArrayList<>();
        rows.add(new Object[] { b2, 20L });
        rows.add(new Object[] { b1, 10L });

        when(repo.findTopBooksByGenreSince(eq("fantasy"), eq(since), any(Pageable.class)))
                .thenReturn(new PageImpl<Object[]>(rows, PageRequest.of(0, 10), rows.size()));


        var res = service.findTopXWithinTopYGenres(10, 1);

        assertThat(res).hasSize(1);
        // Expect [b2, b1] because 20 > 10 (desc order enforced)
        assertThat(res.get(0).books()).containsExactly(b2, b1);
    }

    @Test
    void enforcesTopLimits_topX_and_topY() {
        LocalDate since = LocalDate.now().minusYears(1);

        List<Object[]> allGenreRows = new ArrayList<>();
        allGenreRows.add(new Object[] { "g1", 100L });
        allGenreRows.add(new Object[] { "g2", 90L });
        allGenreRows.add(new Object[] { "g3", 80L });

        when(repo.findTopGenresByLendCountSince(eq(since), any(Pageable.class)))
                .thenAnswer(inv -> {
                    Pageable p = inv.getArgument(1, Pageable.class);
                    int size = Math.min(p.getPageSize(), allGenreRows.size());
                    List<Object[]> pageContent = new ArrayList<>(allGenreRows.subList(0, size));
                    return new PageImpl<Object[]>(pageContent, p, allGenreRows.size());
                });


        // ---- Each genre returns multiple books; with topX=1 we keep only one per genre
        Book b1 = Mockito.mock(Book.class);
        Book b2 = Mockito.mock(Book.class);
        List<Object[]> allBookRows = new ArrayList<>();
        allBookRows.add(new Object[] { b1, 50L });
        allBookRows.add(new Object[] { b2, 49L });

        when(repo.findTopBooksByGenreSince(anyString(), eq(since), any(Pageable.class)))
                .thenAnswer(inv -> {
                    Pageable p = inv.getArgument(2, Pageable.class);
                    int size = Math.min(p.getPageSize(), allBookRows.size());
                    List<Object[]> pageContent = new ArrayList<>(allBookRows.subList(0, size));
                    return new PageImpl<Object[]>(pageContent, p, allBookRows.size());
                });


        var res = service.findTopXWithinTopYGenres(1, 2);

        assertThat(res).hasSize(2);                 // topY enforced
        assertThat(res.get(0).books()).hasSize(1);  // topX enforced
        assertThat(res.get(1).books()).hasSize(1);
    }

    @Test
    void handlesEmptyData_gracefully() {
        LocalDate since = LocalDate.now().minusYears(1);

        Page<Object[]> empty = new PageImpl<Object[]>(
                Collections.<Object[]>emptyList(),
                PageRequest.of(0, 5),
                0
        );
        when(repo.findTopGenresByLendCountSince(eq(since), any(Pageable.class)))
                .thenReturn(empty);

        var res = service.findTopXWithinTopYGenres(3, 5);

        assertThat(res).isEmpty();
    }
}
