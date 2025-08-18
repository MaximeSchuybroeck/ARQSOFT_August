package pt.psoft.g1.psoftg1.lendingmanagement.model;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.hibernate.StaleObjectStateException;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class LendingWhiteBoxTest {

    private Book anyBook() {
        return mock(Book.class);
    }

    private ReaderDetails anyReaderDetails() {
        return mock(ReaderDetails.class);
    }

    @Test
    void setReturned_twice_throwsAndKeepsFirstReturnDate() {
        // constructor: (Book, ReaderDetails, seq, lendingDuration, fineValuePerDayInCents)
        Lending l = new Lending(anyBook(), anyReaderDetails(), 1, 14, 50);

        // init-version is 0 → correct call
        l.setReturned(0L, "all good");
        LocalDate first = (LocalDate) ReflectionTestUtils.getField(l, "returnedDate");
        assertNotNull(first, "first return should set returnedDate");

        // second time has to fail and leave data unchanged
        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> l.setReturned(0L, "again"));
        assertTrue(ex.getMessage().toLowerCase().contains("already"), "should indicate already returned");
        LocalDate second = (LocalDate) ReflectionTestUtils.getField(l, "returnedDate");
        assertEquals(first, second, "returnedDate must not change on second call");
    }

    @Test
    void setReturned_withWrongVersion_throwsStaleObject() {
        Lending l = new Lending(anyBook(), anyReaderDetails(), 1, 14, 50);
        assertThrows(StaleObjectStateException.class, () -> l.setReturned(1L, "wrong version"));
    }

    @Test
    void getDaysUntilReturn_presentWhenNotReturned_absentWhenReturned() {
        // Non-returned: must be present (>=0)
        Lending active = new Lending(anyBook(), anyReaderDetails(), 1, 7, 50);
        Optional<Integer> left = active.getDaysUntilReturn();
        assertTrue(left.isPresent());
        assertTrue(left.get() >= 0);

        // Returned with factory: must be empty
        LocalDate start = LocalDate.now().minusDays(10);
        Lending done = Lending.newBootstrappingLending(
                anyBook(), anyReaderDetails(), 2025, 42, start, LocalDate.now(), 7, 50);
        Optional<Integer> leftAfter = done.getDaysUntilReturn();
        assertTrue(leftAfter.isEmpty(), "daysUntilReturn should be empty for returned lendings");
    }

    @Test
    void getDaysOverdue_calculatesFromDates() {
        LocalDate start = LocalDate.now().minusDays(10);
        // duration 7 → limit = start+7 = now-3; returns today → overdue = 3
        Lending done = Lending.newBootstrappingLending(
                anyBook(), anyReaderDetails(), 2025, 7, start, LocalDate.now(), 7, 50);
        Optional<Integer> overdue = done.getDaysOverdue();
        assertTrue(overdue.isPresent());
        assertEquals(3, overdue.get());
    }

    @Test
    void getDaysDelayed_matchesOverdueWhenReturnedToday() {
        LocalDate start = LocalDate.now().minusDays(15);
        int duration = 10; // limit = nu-5
        // Not yet returned
        Lending l = Lending.newBootstrappingLending(
                anyBook(), anyReaderDetails(), 2025, 9, start, null, duration, 50);
        int delayedBefore = l.getDaysDelayed(); // nu - limit = ~5
        assertTrue(delayedBefore >= 5);

        // Return with correct version (0)
        l.setReturned(0L, "ok");
        int delayedAfter = l.getDaysDelayed(); // returnedDate - limit ≈ 5
        assertTrue(delayedAfter >= 5);
    }
}
