package com.example.lending_service.integration;

import com.example.lending_service.client.Client;
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
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LendingController {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @Autowired RecommendationRepository recRepo;

    @MockBean Client.BookClient bookClient;
    @MockBean Client.ReaderClient readerClient;
    @MockBean RabbitTemplate rabbitTemplate; // avoid real broker

    @BeforeEach
    void stubs() {
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

        // Second return -> your controller maps IllegalStateException/IllegalArgumentException appropriately
        mvc.perform(post("/api/lendings/return/{id}", lendingId))
                .andExpect(status().isConflict()); // already returned
    }
}
