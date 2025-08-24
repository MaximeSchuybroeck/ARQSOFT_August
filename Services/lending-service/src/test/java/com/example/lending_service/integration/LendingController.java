package com.example.lending_service.integration;

import com.example.lending_service.client.Client;
import com.example.lending_service.entity.Lending;
import com.example.lending_service.entity.Recommendation;
import com.example.lending_service.repository.LendingRepository;
import com.example.lending_service.repository.RecommendationRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        // keep UrlLog / title resolver happy
        "book.service.url=http://book-service:8083"
})
class LendingController {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @Autowired LendingRepository lendingRepo;
    @Autowired RecommendationRepository recRepo;

    @MockBean Client.BookClient bookClient;
    @MockBean Client.ReaderClient readerClient;
    @MockBean RabbitTemplate rabbitTemplate; // avoid real broker

    @BeforeEach
    void resetDbAndStubs() {
        // clear data between tests (delete in FK-safe order)
        recRepo.deleteAll();
        lendingRepo.deleteAll();

        // stubs
        given(bookClient.bookExists(anyLong())).willReturn(true);
        given(readerClient.readerExists(anyString())).willReturn(true);
    }

    // Integration #1: borrow -> return with positive recommendation -> summary shows 1 positive
    @Test
    void flow_borrow_then_return_with_positive_then_summary() throws Exception {
        // borrow
        String borrowJson = mvc.perform(post("/api/lendings/borrow")
                        .param("bookId", "1")
                        .param("readerEmail", "alice@example.com"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        JsonNode borrowed = om.readTree(borrowJson);
        long lendingId = borrowed.get("id").asLong();

        // return with positive recommendation + comment
        mvc.perform(post("/api/lendings/return/{id}", lendingId)
                        .param("recommended", "true")
                        .param("comment", "Loved it"))
                .andExpect(status().isOk());

        // summary must show 1 positive
        String summaryJson = mvc.perform(get("/api/lendings/books/{bookId}/recommendations/summary", 1))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode summary = om.readTree(summaryJson);
        assertThat(summary.get("bookId").asLong()).isEqualTo(1L);
        assertThat(summary.get("positives").asInt()).isEqualTo(1);
        assertThat(summary.get("negatives").asInt()).isEqualTo(0);
        assertThat(summary.get("total").asInt()).isEqualTo(1);
        assertThat(summary.get("positiveRatio").asDouble()).isEqualTo(1.0d);
    }

    // Integration #2: returning the same lending twice yields 409 (from @ExceptionHandler mapping)
    @Test
    void returningSameLendingTwice_isConflict() throws Exception {
        String borrowJson = mvc.perform(post("/api/lendings/borrow")
                        .param("bookId", "2")
                        .param("readerEmail", "bob@example.com"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long lendingId = om.readTree(borrowJson).get("id").asLong();

        mvc.perform(post("/api/lendings/return/{id}", lendingId)
                        .param("recommended", "false")
                        .param("comment", "Not for me"))
                .andExpect(status().isOk());

        // Second return -> 409 Conflict (already returned)
        mvc.perform(post("/api/lendings/return/{id}", lendingId))
                .andExpect(status().isConflict());
    }

    @Test
    void negative_recommendation_is_persisted() throws Exception {
        var toSave = new Lending();
        toSave.setBookId(301L);
        toSave.setReaderEmail("neg@example.com");
        toSave.setStartDate(LocalDate.now());
        toSave.setDueDate(LocalDate.now().plusDays(10));

        var saved = lendingRepo.save(toSave);

        mvc.perform(post("/api/lendings/return/{id}", saved.getId())
                        .param("recommended", "false")
                        .param("comment", "Not my style"))
                .andExpect(status().isOk());

        final Long lendingId = saved.getId();

        var allForThisLending = recRepo.findAll().stream()
                .filter(r -> Objects.equals(r.getLendingId(), lendingId))
                .toList();

        assertThat(allForThisLending).hasSize(1);
        var r = allForThisLending.get(0);
        assertThat(r.getBookId()).isEqualTo(301L);
        assertThat(r.getLendingId()).isEqualTo(lendingId);
        assertThat(r.getSentiment().name()).isEqualTo("NEGATIVE");
        assertThat(r.getComment()).isEqualTo("Not my style");
    }


    @Test
    void duplicate_recommendation_for_same_lending_is_rejected() throws Exception {
        var lend = new Lending();
        lend.setBookId(302L);
        lend.setReaderEmail("dup@example.com");
        lend.setStartDate(LocalDate.now());
        lend.setDueDate(LocalDate.now().plusDays(5));
        lend = lendingRepo.save(lend);

        // first return with recommendation
        mvc.perform(post("/api/lendings/return/{id}", lend.getId())
                        .param("recommended", "true"))
                .andExpect(status().isOk());

        // second try: lending is no longer active -> 409 Conflict (already returned)
        mvc.perform(post("/api/lendings/return/{id}", lend.getId())
                        .param("recommended", "true"))
                .andExpect(status().isConflict());
    }
}
