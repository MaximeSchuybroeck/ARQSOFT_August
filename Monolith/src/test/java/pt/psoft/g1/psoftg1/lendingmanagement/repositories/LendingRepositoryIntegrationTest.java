package pt.psoft.g1.psoftg1.lendingmanagement.repositories;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;
import pt.psoft.g1.psoftg1.PsoftG1Application;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.authormanagement.repositories.AuthorRepository;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.bookmanagement.repositories.BookRepository;
import pt.psoft.g1.psoftg1.configuration.SecurityConfig;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.genremanagement.repositories.GenreRepository;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Lending;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.readermanagement.repositories.ReaderRepository;
import pt.psoft.g1.psoftg1.shared.services.Page;
import pt.psoft.g1.psoftg1.usermanagement.model.Reader;
import pt.psoft.g1.psoftg1.usermanagement.repositories.UserRepository;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Transactional
@TestPropertySource(properties = {
        "spring.security.oauth2.client.registration.google.client-id=dummy",
        "spring.security.oauth2.client.registration.google.client-secret=dummy"
})
@SpringBootTest
public class LendingRepositoryIntegrationTest {

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

    private String generateRandomIsbn() {
        return "9780134685991"; // ISBN for "Effective Java"
    }


    private int generateUniqueSeq() {
        return (int) (System.nanoTime() % 1_000_000);
    }

    @BeforeEach
    public void setUp() {
        // Clean DB before every test
//        lendingRepository.deleteAll();
//        readerRepository.deleteAll();
//        userRepository.deleteAll();
//        bookRepository.deleteAll();
//        genreRepository.deleteAll();
//        authorRepository.deleteAll();

        // Create dependencies
        author = new Author("Author " + UUID.randomUUID(), "Test Author Bio", null);
        authorRepository.save(author);

        genre = new Genre("Genre " + UUID.randomUUID());
        genreRepository.save(genre);

        book = new Book(generateRandomIsbn(), "Book Title", "Description", genre, List.of(author), null);
        bookRepository.save(book);

        reader = Reader.newReader(UUID.randomUUID() + "@mail.com", "Password123!", "Reader Name");
        reader.setAge(25); // ✅ required field to pass validation
        userRepository.save(reader);

        int randomReaderNumber = new Random().nextInt(10_000);
        readerDetails = new ReaderDetails(randomReaderNumber, reader, "2000-01-01", "919191919", true, true, true, null, null);
        readerRepository.save(readerDetails);

        int uniqueSeq = generateUniqueSeq();
        lending = Lending.newBootstrappingLending(book, readerDetails, LocalDate.now().getYear(), uniqueSeq,
                LocalDate.now().minusDays(10), LocalDate.now(), 10, 200);

        // Set unique fields manually
        ReflectionTestUtils.setField(lending, "customIncrementalId", System.nanoTime());
        ReflectionTestUtils.setField(lending, "hexId", UUID.randomUUID().toString().replace("-", "").substring(0, 24));
        ReflectionTestUtils.setField(lending, "businessId", UUID.randomUUID().toString().replace("-", "").substring(0, 20));

        lendingRepository.save(lending);
    }

    @Test
    public void testSave() {
        Lending newLending = Lending.newBootstrappingLending(book, readerDetails, LocalDate.now().getYear(), generateUniqueSeq(),
                LocalDate.now().minusDays(5), LocalDate.now(), 10, 100);
        ReflectionTestUtils.setField(newLending, "customIncrementalId", System.nanoTime());
        ReflectionTestUtils.setField(newLending, "hexId", UUID.randomUUID().toString().replace("-", "").substring(0, 24));
        ReflectionTestUtils.setField(newLending, "businessId", UUID.randomUUID().toString().replace("-", "").substring(0, 20));

        Lending saved = lendingRepository.save(newLending);
        assertThat(saved).isNotNull();
        assertThat(saved.getLendingNumber()).isEqualTo(newLending.getLendingNumber());
    }

    @Test
    public void testFindByLendingNumber() {
        Optional<Lending> found = lendingRepository.findByLendingNumber(lending.getLendingNumber());
        assertThat(found).isPresent();
        assertThat(found.get().getLendingNumber()).isEqualTo(lending.getLendingNumber());
    }

    @Test
    public void testListByReaderNumberAndIsbn() {
        List<Lending> lendings = lendingRepository.listByReaderNumberAndIsbn(
                lending.getReaderDetails().getReaderNumber(), lending.getBook().getIsbn());
        assertThat(lendings).isNotEmpty().contains(lending);
    }

    @Test
    public void testGetCountFromCurrentYear() {
        int count = lendingRepository.getCountFromCurrentYear();
        assertThat(count).isGreaterThan(0);

        var lending2 = Lending.newBootstrappingLending(book, readerDetails, LocalDate.now().getYear(), generateUniqueSeq(),
                LocalDate.now(), null, 15, 300);
        ReflectionTestUtils.setField(lending2, "customIncrementalId", System.nanoTime());
        ReflectionTestUtils.setField(lending2, "hexId", UUID.randomUUID().toString().replace("-", "").substring(0, 24));
        ReflectionTestUtils.setField(lending2, "businessId", UUID.randomUUID().toString().replace("-", "").substring(0, 20));
        lendingRepository.save(lending2);

        int newCount = lendingRepository.getCountFromCurrentYear();
        assertThat(newCount).isGreaterThan(count);
    }

    @Test
    public void testListOutstandingByReaderNumber() {
        var lending2 = Lending.newBootstrappingLending(book, readerDetails, LocalDate.now().getYear(), generateUniqueSeq(),
                LocalDate.now().minusDays(7), null, 15, 300);
        ReflectionTestUtils.setField(lending2, "customIncrementalId", System.nanoTime());
        ReflectionTestUtils.setField(lending2, "hexId", UUID.randomUUID().toString().replace("-", "").substring(0, 24));
        ReflectionTestUtils.setField(lending2, "businessId", UUID.randomUUID().toString().replace("-", "").substring(0, 20));
        lendingRepository.save(lending2);

        List<Lending> outstanding = lendingRepository.listOutstandingByReaderNumber(readerDetails.getReaderNumber());
        assertThat(outstanding).contains(lending2);
    }

    @Test
    public void testGetAverageDuration() {
        double dur1 = ChronoUnit.DAYS.between(lending.getStartDate(), lending.getReturnedDate());
        double avg = lendingRepository.getAverageDuration();
        assertEquals(dur1, avg, 0.01);

        var lending2 = Lending.newBootstrappingLending(book, readerDetails, LocalDate.now().getYear(), generateUniqueSeq(),
                LocalDate.now().minusDays(20), LocalDate.now().minusDays(10), 15, 300);
        ReflectionTestUtils.setField(lending2, "customIncrementalId", System.nanoTime());
        ReflectionTestUtils.setField(lending2, "hexId", UUID.randomUUID().toString().replace("-", "").substring(0, 24));
        ReflectionTestUtils.setField(lending2, "businessId", UUID.randomUUID().toString().replace("-", "").substring(0, 20));
        lendingRepository.save(lending2);

        double dur2 = ChronoUnit.DAYS.between(lending2.getStartDate(), lending2.getReturnedDate());
        double expectedAvg = (dur1 + dur2) / 2.0;
        assertEquals(expectedAvg, lendingRepository.getAverageDuration(), 0.01);
    }

    @Test
    public void testGetOverdue() {
        var overdue = Lending.newBootstrappingLending(book, readerDetails, LocalDate.now().getYear(), generateUniqueSeq(),
                LocalDate.now().minusDays(30), null, 15, 300);
        ReflectionTestUtils.setField(overdue, "customIncrementalId", System.nanoTime());
        ReflectionTestUtils.setField(overdue, "hexId", UUID.randomUUID().toString().replace("-", "").substring(0, 24));
        ReflectionTestUtils.setField(overdue, "businessId", UUID.randomUUID().toString().replace("-", "").substring(0, 20));
        lendingRepository.save(overdue);

        Page page = new Page(1, 10);
        List<Lending> overdueList = lendingRepository.getOverdue(page);
        assertThat(overdueList).contains(overdue);
    }
}
