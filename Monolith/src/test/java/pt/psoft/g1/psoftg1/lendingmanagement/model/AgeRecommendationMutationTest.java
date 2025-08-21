package pt.psoft.g1.psoftg1.lendingmanagement.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.psoft.g1.psoftg1.authormanagement.repositories.AuthorRepository;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.bookmanagement.repositories.BookRepository;
import pt.psoft.g1.psoftg1.bookmanagement.services.BookServiceImpl;
import pt.psoft.g1.psoftg1.configuration.AgeThresholdProperties;
import pt.psoft.g1.psoftg1.genremanagement.repositories.GenreRepository;
import pt.psoft.g1.psoftg1.lendingmanagement.repositories.LendingRepository;
import pt.psoft.g1.psoftg1.readermanagement.repositories.ReaderRepository;
import pt.psoft.g1.psoftg1.shared.repositories.PhotoRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AgeRecommendationMutationTest {

    @Mock BookRepository bookRepository;
    @Mock GenreRepository genreRepository;
    @Mock AuthorRepository authorRepository;
    @Mock PhotoRepository photoRepository;
    @Mock ReaderRepository readerRepository;
    @Mock LendingRepository lendingRepository;

    BookServiceImpl service;

    @BeforeEach
    void init() {
        AgeThresholdProperties props = new AgeThresholdProperties();
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
    void age_9_is_children_but_age_10_is_juvenile_boundary() {
        List<Book> children = List.of(mock(Book.class));
        List<Book> juvenile = List.of(mock(Book.class));
        when(bookRepository.findByGenre_GenreIgnoreCase("children")).thenReturn(children);
        when(bookRepository.findByGenre_GenreIgnoreCase("juvenile")).thenReturn(juvenile);

        assertThat(service.recommendBooksByAge(9, 1L)).isEqualTo(children);
        assertThat(service.recommendBooksByAge(10, 1L)).isEqualTo(juvenile); // dwingt < vs <= correct
    }

    @Test
    void age_17_is_juvenile_but_age_18_switches_to_adult_path() {
        List<Book> juvenile = List.of(mock(Book.class));
        List<Book> fantasy = List.of(mock(Book.class));
        when(bookRepository.findByGenre_GenreIgnoreCase("juvenile")).thenReturn(juvenile);
        when(lendingRepository.findMostLentGenreByReader(anyLong())).thenReturn("fantasy");
        when(bookRepository.findByGenre_GenreIgnoreCase("fantasy")).thenReturn(fantasy);

        assertThat(service.recommendBooksByAge(17, 7L)).isEqualTo(juvenile);
        assertThat(service.recommendBooksByAge(18, 7L)).isEqualTo(fantasy); // dwingt < vs <= correct
    }
}
