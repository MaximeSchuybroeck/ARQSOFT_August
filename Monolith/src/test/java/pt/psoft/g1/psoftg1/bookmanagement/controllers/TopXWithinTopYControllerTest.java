package pt.psoft.g1.psoftg1.bookmanagement.controllers; // <- your test package

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import pt.psoft.g1.psoftg1.bookmanagement.api.RecommendationController;
import pt.psoft.g1.psoftg1.bookmanagement.services.BookServiceImpl;
import pt.psoft.g1.psoftg1.bookmanagement.services.TopByGenreDTO;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = RecommendationController.class)
@AutoConfigureMockMvc(addFilters = false)
class TopXWithinTopYControllerTest {

    @Autowired MockMvc mvc;

    // IMPORTANT: mock the exact type the controller injects (BookServiceImpl)
    @MockBean BookServiceImpl bookService;

    @Test
    void returnsTopXWithinTopYGenresJson_structureOnly() throws Exception {
        Book f1 = org.mockito.Mockito.mock(Book.class);
        Book f2 = org.mockito.Mockito.mock(Book.class);
        Book s1 = org.mockito.Mockito.mock(Book.class);

        var payload = List.of(
                new TopByGenreDTO("fantasy", List.of(f1, f2)),
                new TopByGenreDTO("sci-fi", List.of(s1))
        );
        when(bookService.findTopXWithinTopYGenres(2, 2)).thenReturn(payload);

        mvc.perform(get("/api/recommendations/topxy")
                        .param("x", "2")
                        .param("y", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].genre").value("fantasy"))
                .andExpect(jsonPath("$[0].books.length()").value(2))
                .andExpect(jsonPath("$[1].genre").value("sci-fi"))
                .andExpect(jsonPath("$[1].books.length()").value(1));
    }
}
