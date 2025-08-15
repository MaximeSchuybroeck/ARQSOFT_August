package pt.psoft.g1.psoftg1.lendingmanagement.services;

import org.hibernate.StaleObjectStateException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;
import pt.psoft.g1.psoftg1.TestSecurityBeansConfig;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.authormanagement.repositories.AuthorRepository;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.bookmanagement.repositories.BookRepository;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.genremanagement.repositories.GenreRepository;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Lending;
import pt.psoft.g1.psoftg1.lendingmanagement.repositories.LendingRepository;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.readermanagement.repositories.ReaderRepository;
import pt.psoft.g1.psoftg1.usermanagement.model.Reader;
import pt.psoft.g1.psoftg1.usermanagement.repositories.UserRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;


@Transactional
@SpringBootTest
@ComponentScan(basePackages = "pt.psoft.g1.psoftg1")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestSecurityBeansConfig.class)
public class LendingServiceImplTest {

    @Autowired
    private LendingService lendingService;

    @Autowired
    private LendingRepository lendingRepository;

    @Autowired
    private ReaderRepository readerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private AuthorRepository authorRepository;

    private Lending lending;
    private ReaderDetails readerDetails;
    private Reader reader;
    private Book book;
    private Author author;
    private Genre genre;

    @BeforeEach
    void setUp() {
        author = new Author("Manuel Antonio Pina",
                "Manuel António Pina foi um jornalista e escritor português, premiado em 2011 com o Prémio Camões",
                null);
        authorRepository.save(author);

        genre = new Genre("Género");
        genreRepository.save(genre);

        List<Author> authors = List.of(author);
        book = new Book("9782826012092",
                "O Inspetor Max",
                "conhecido pastor-alemão que trabalha para a Judiciária, vai ser fundamental para resolver um importante caso de uma rede de malfeitores que quer colocar uma bomba num megaconcerto de uma ilustre cantora",
                genre,
                authors,
                null);
        bookRepository.save(book);

        reader = Reader.newReader("manuel@gmail.com", "Manuelino123!", "Manuel Sarapinto das Coives");
        reader.setAge(24); // ✅ Set age to satisfy @NotNull constraint
        userRepository.save(reader);

        readerDetails = new ReaderDetails(
                new Random().nextInt(10000),  // ensures uniqueness
                reader,
                "2000-01-01",
                "919191919",
                true,
                true,
                true,
                null, null
        );
        readerRepository.save(readerDetails);
        final AtomicLong testIncrementalCounter = new AtomicLong(1000); // Start from a large number
        lending = Lending.newBootstrappingLending(
                book,
                readerDetails,
                LocalDate.now().getYear(),
                999,
                LocalDate.of(LocalDate.now().getYear(), 1, 1),
                LocalDate.of(LocalDate.now().getYear(), 1, 11),
                15,
                300
        );
        ReflectionTestUtils.setField(lending, "customIncrementalId", testIncrementalCounter.getAndIncrement());
        lendingRepository.save(lending);

    }
    @AfterEach
    void tearDown() {
        lendingRepository.delete(lending);
        readerRepository.delete(readerDetails);
        userRepository.delete(reader);
        bookRepository.delete(book);
        genreRepository.delete(genre);
        authorRepository.delete(author);
    }

    @Test
    void testFindByLendingNumber() {
        String validLendingNumber = LocalDate.now().getYear() + "/999";
        String invalidLendingNumber = LocalDate.now().getYear() + "/1";

        assertThat(lendingService.findByLendingNumber(validLendingNumber)).isPresent();
        assertThat(lendingService.findByLendingNumber(invalidLendingNumber)).isEmpty();
    }

    @Test
    void testSetReturned() {
        int year = 2024, seq = 888;
        var notReturnedLending = lendingRepository.save(Lending.newBootstrappingLending(book,
                readerDetails,
                year,
                seq,
                LocalDate.of(2024, 3,1),
                null,
                15,
                300));
        var request = new SetLendingReturnedRequest(null);
        assertThrows(StaleObjectStateException.class,
                () -> lendingService.setReturned(year + "/" + seq, request, (notReturnedLending.getVersion()-1)));

        assertDoesNotThrow(
                () -> lendingService.setReturned(year + "/" + seq, request, notReturnedLending.getVersion()));
    }
}

