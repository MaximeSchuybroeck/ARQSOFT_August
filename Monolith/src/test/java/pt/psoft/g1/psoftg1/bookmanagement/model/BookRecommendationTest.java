package pt.psoft.g1.psoftg1.bookmanagement.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.bookmanagement.repositories.BookRepository;
import pt.psoft.g1.psoftg1.bookmanagement.services.BookServiceImpl;
import pt.psoft.g1.psoftg1.configuration.AgeThresholdProperties;
import pt.psoft.g1.psoftg1.genremanagement.repositories.GenreRepository;
import pt.psoft.g1.psoftg1.authormanagement.repositories.AuthorRepository;
import pt.psoft.g1.psoftg1.readermanagement.repositories.ReaderRepository;
import pt.psoft.g1.psoftg1.lendingmanagement.repositories.LendingRepository;
import pt.psoft.g1.psoftg1.shared.repositories.PhotoRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookRecommendationTest {

    @Mock BookRepository bookRepository;
    @Mock GenreRepository genreRepository;
    @Mock AuthorRepository authorRepository;
    @Mock PhotoRepository photoRepository;
    @Mock ReaderRepository readerRepository;
    @Mock LendingRepository lendingRepository;

    AgeThresholdProperties props;

    @InjectMocks
    BookServiceImpl service;

    @BeforeEach
    void setUp() {
        props = new AgeThresholdProperties();
        props.setChild(10);
        props.setJuvenile(18);
        service = new BookServiceImpl(
                bookRepository,
                genreRepository,
                authorRepository,
                photoRepository,
                readerRepository,
                props,
                lendingRepository
        );
    }

    @Test
    void recommend_whenUnderChildThreshold_returnsChildrenGenre() {
        List<Book> children = List.of(mock(Book.class), mock(Book.class));
        when(bookRepository.findByGenre_GenreIgnoreCase("children")).thenReturn(children);

        var result = service.recommendBooksByAge(9, 42L);

        assertEquals(children, result);
        verify(bookRepository).findByGenre_GenreIgnoreCase("children");
        verifyNoInteractions(lendingRepository); // waarom: kindpad gebruikt geen historie
    }

    @Test
    void recommend_whenUnderJuvenileThreshold_returnsJuvenileGenre() {
        List<Book> juvenile = List.of(mock(Book.class));
        when(bookRepository.findByGenre_GenreIgnoreCase("juvenile")).thenReturn(juvenile);

        var result = service.recommendBooksByAge(12, 42L);

        assertEquals(juvenile, result);
        verify(bookRepository).findByGenre_GenreIgnoreCase("juvenile");
        verifyNoInteractions(lendingRepository);
    }

    @Test
    void recommend_whenAdult_usesMostLentGenreOfReader() {
        List<Book> fantasy = List.of(mock(Book.class));
        when(lendingRepository.findMostLentGenreByReader(anyLong())).thenReturn("fantasy");
        when(bookRepository.findByGenre_GenreIgnoreCase("fantasy")).thenReturn(fantasy);

        var result = service.recommendBooksByAge(25, 99L);

        assertEquals(fantasy, result);
        verify(lendingRepository).findMostLentGenreByReader(99L);
        verify(bookRepository).findByGenre_GenreIgnoreCase("fantasy");
    }
}
