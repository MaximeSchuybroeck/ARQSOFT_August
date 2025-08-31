package com.example.lending_service.acceptance;

import com.example.lending_service.LendingServiceApplication;
import com.example.lending_service.client.Client;
import com.example.lending_service.entity.Lending;
import com.example.lending_service.repository.LendingRepository;
import com.example.lending_service.repository.RecommendationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = LendingServiceApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        // make UrlLog/book title resolver happy during tests
        "book.service.url=http://book-service:8083"
})
public class RecommendationSummary {

    @Autowired MockMvc mvc;
    @Autowired LendingRepository lendingRepo;
    @Autowired RecommendationRepository recRepo;

    @MockBean Client.BookClient bookClient;
    @MockBean Client.ReaderClient readerClient;
    @MockBean RabbitTemplate rabbitTemplate;

    @BeforeEach
    void setup() {
        lendingRepo.deleteAll();
        recRepo.deleteAll();
        Mockito.when(bookClient.bookExists(anyLong())).thenReturn(true);
        Mockito.when(readerClient.readerExists(anyString())).thenReturn(true);
    }

    @Test
    void return_with_positive_recommendation_updates_summary_and_allows_borrow_again() throws Exception {
        long bookId = 101L;
        String email = "reader@example.com";

        // Seed: active lending
        Lending l = new Lending();
        l.setBookId(bookId);
        l.setReaderEmail(email);
        l.setStartDate(LocalDate.now());
        l.setDueDate(LocalDate.now().plusDays(14));
        l = lendingRepo.save(l);

        // Return with recommendation=true
        mvc.perform(post("/api/lendings/return/{id}", l.getId())
                        .param("recommended", "true")
                        .param("comment", "Great read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.returnedDate").exists());

        // Summary should show the positive
        mvc.perform(get("/api/lendings/books/{bookId}/recommendations/summary", bookId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookId").value((int) bookId))
                .andExpect(jsonPath("$.positives").value(1))
                .andExpect(jsonPath("$.negatives").value(0))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.positiveRatio").value(1.0));

        // Borrow again should now be possible (book not active anymore)
        mvc.perform(post("/api/lendings/borrow")
                        .param("bookId", String.valueOf(bookId))
                        .param("readerEmail", email))
                .andExpect(status().isCreated());
    }

    @Test
    void return_without_recommendation_does_not_change_summary() throws Exception {
        long bookId = 202L;
        String email = "reader2@example.com";

        Lending l = new Lending();
        l.setBookId(bookId);
        l.setReaderEmail(email);
        l.setStartDate(LocalDate.now());
        l.setDueDate(LocalDate.now().plusDays(7));
        l = lendingRepo.save(l);

        // Return with NO recommended param
        mvc.perform(post("/api/lendings/return/{id}", l.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.returnedDate").exists());

        // Summary stays 0
        mvc.perform(get("/api/lendings/books/{bookId}/recommendations/summary", bookId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.positives").value(0))
                .andExpect(jsonPath("$.negatives").value(0))
                .andExpect(jsonPath("$.total").value(0))
                .andExpect(jsonPath("$.positiveRatio").value(0.0));

        // And double return should fail (already returned) -> 400 mapped by controller
        mvc.perform(post("/api/lendings/return/{id}", l.getId()))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("already returned")));
    }
}
