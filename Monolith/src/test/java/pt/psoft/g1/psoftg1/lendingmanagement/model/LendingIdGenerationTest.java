package pt.psoft.g1.psoftg1.lendingmanagement.model;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class LendingIdGenerationTest {

    @Test
    void constructor_setsOrAllowsIds_withCorrectFormats() {
        Book book = mock(Book.class);
        ReaderDetails readerDetails = mock(ReaderDetails.class);

        Lending l = new Lending(book, readerDetails, 1, 14, 50);

        String hexId = (String) ReflectionTestUtils.getField(l, "hexId");
        String businessId = (String) ReflectionTestUtils.getField(l, "businessId");
        Long customInc = (Long) ReflectionTestUtils.getField(l, "customIncrementalId");

        assertNotNull(hexId, "hexId must be generated");
        assertTrue(hexId.matches("^[0-9a-fA-F]{24}$"), "hexId must be 24 hex chars");

        if (businessId != null) {
            assertTrue(businessId.matches("^[A-Za-z0-9]{20}$"), "businessId must be 20 alphanumeric");
        }
        if (customInc != null) {
            assertTrue(customInc >= 0L);
        }

        // extra: de publieke generate* methods moeten correcte formats leveren
        assertTrue(l.generateHexId().matches("^[0-9a-fA-F]{24}$"));
        assertTrue(l.generateBusinessId(hexId).matches("^[A-Za-z0-9]{20}$"));
        assertNotNull(l.generateCustomIncrementalId());
    }
}
