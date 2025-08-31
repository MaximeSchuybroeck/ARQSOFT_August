package com.example.lending_service.acceptance;

import com.example.lending_service.client.Client;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ReturnRecommendation {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockBean Client.BookClient bookClient;
    @MockBean Client.ReaderClient readerClient;
    @MockBean RabbitTemplate rabbitTemplate;

    @BeforeEach
    void stubs() {
        given(bookClient.bookExists(anyLong())).willReturn(true);
        given(readerClient.readerExists(anyString())).willReturn(true);
    }

    // Acceptance #1: Reader returns with a NEGATIVE recommendation and a comment. Summary reflects it.
    @Test
    void readerCanReturnWithNegativeRecommendationAndComment() throws Exception {
        String borrowJson = mvc.perform(post("/api/lendings/borrow")
                        .param("bookId", "3")
                        .param("readerEmail", "carol@example.com"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long lendingId = om.readTree(borrowJson).get("id").asLong();

        mvc.perform(post("/api/lendings/return/{id}", lendingId)
                        .param("recommended", "false")
                        .param("comment", "Too slow for me"))
                .andExpect(status().isOk());

        String summaryJson = mvc.perform(get("/api/lendings/books/{bookId}/recommendations/summary", 3))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode s = om.readTree(summaryJson);
        assertThat(s.get("negatives").asInt()).isEqualTo(1);
        assertThat(s.get("total").asInt()).isEqualTo(1);
    }

    // Acceptance #2: Reader returns without any recommendation. Summary stays unchanged.
    @Test
    void readerCanReturnWithoutRecommendation() throws Exception {
        String borrowJson = mvc.perform(post("/api/lendings/borrow")
                        .param("bookId", "4")
                        .param("readerEmail", "dave@example.com"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long lendingId = om.readTree(borrowJson).get("id").asLong();

        // No 'recommended' param at all
        mvc.perform(post("/api/lendings/return/{id}", lendingId))
                .andExpect(status().isOk());

        String summaryJson = mvc.perform(get("/api/lendings/books/{bookId}/recommendations/summary", 4))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode s = om.readTree(summaryJson);
        assertThat(s.get("total").asInt()).isEqualTo(0);
        assertThat(s.get("positives").asInt()).isEqualTo(0);
        assertThat(s.get("negatives").asInt()).isEqualTo(0);
    }

}
