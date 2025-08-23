package pt.psoft.g1.psoftg1.authormanagement.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AuthorIdGenerationFunctionsTest {

    @Test
    void generateMethods_produceExpectedFormats() {
        Author a = new Author("Ada Lovelace", "Pioneer of computing.", null);

        String hex = a.generateHexId();
        assertNotNull(hex);
        assertTrue(hex.matches("^[0-9a-fA-F]{24}$"));

        String business = a.generateBusinessId(hex);
        assertNotNull(business);
        assertTrue(business.matches("^[A-Za-z0-9]{20}$"));

        Long inc1 = a.generateCustomIncrementalId();
        Long inc2 = a.generateCustomIncrementalId();
        assertNotNull(inc1);
        assertNotNull(inc2);
        assertTrue(inc2 > inc1); // waarom: monotone toename verwacht
    }
}
