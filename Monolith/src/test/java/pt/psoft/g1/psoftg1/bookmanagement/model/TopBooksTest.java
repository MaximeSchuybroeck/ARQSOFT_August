package pt.psoft.g1.psoftg1.bookmanagement.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import pt.psoft.g1.psoftg1.bookmanagement.repositories.BookRepository;
import pt.psoft.g1.psoftg1.bookmanagement.services.BookCountDTO;
import pt.psoft.g1.psoftg1.bookmanagement.services.BookServiceImpl;
import pt.psoft.g1.psoftg1.genremanagement.repositories.GenreRepository;
import pt.psoft.g1.psoftg1.authormanagement.repositories.AuthorRepository;
import pt.psoft.g1.psoftg1.readermanagement.repositories.ReaderRepository;
import pt.psoft.g1.psoftg1.shared.repositories.PhotoRepository;
import pt.psoft.g1.psoftg1.lendingmanagement.repositories.LendingRepository;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TopBooksTest {

    @Mock BookRepository bookRepository;
    @Mock GenreRepository genreRepository;
    @Mock AuthorRepository authorRepository;
    @Mock PhotoRepository photoRepository;
    @Mock ReaderRepository readerRepository;
    @Mock LendingRepository lendingRepository;

    BookServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new BookServiceImpl(bookRepository, genreRepository, authorRepository, photoRepository, readerRepository, new pt.psoft.g1.psoftg1.configuration.AgeThresholdProperties(), lendingRepository);
    }

    @Test
    void findTop5BooksLent_callsRepositoryWithOneYearWindow_andReturnsContent() {
        Page<BookCountDTO> page = new PageImpl<>(List.of(new BookCountDTO(null, 10)));
        when(bookRepository.findTop5BooksLent(any(LocalDate.class), any(Pageable.class))).thenReturn(page);

        var result = service.findTop5BooksLent();

        assertEquals(1, result.size());
        assertEquals(10, result.get(0).getLendingCount());
        verify(bookRepository).findTop5BooksLent(any(LocalDate.class), any(Pageable.class));
    }
}
