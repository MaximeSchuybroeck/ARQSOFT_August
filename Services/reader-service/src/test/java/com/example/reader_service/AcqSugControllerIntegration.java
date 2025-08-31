package com.example.reader_service;

import com.example.reader_service.dto.AcqSugRequestDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AcqSugControllerIntegration {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/suggestions: creates a suggestion and returns 201 with payload")
    void suggest_createAndReturn201() throws Exception {
        AcqSugRequestDTO request = new AcqSugRequestDTO();
        request.setTitle("Clean Architecture");
        request.setGenre("Programming");
        request.setAuthorFirstName("Robert");
        request.setAuthorLastName("Martin");

        mockMvc.perform(post("/api/suggestions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.title", is("Clean Architecture")))
                .andExpect(jsonPath("$.genre", is("Programming")))
                .andExpect(jsonPath("$.authorFirstName", is("Robert")))
                .andExpect(jsonPath("$.authorLastName", is("Martin")))
                .andExpect(jsonPath("$.status", is("NEW")))
                .andExpect(jsonPath("$.timesSuggested", is(1)));
    }

    @Test
    @DisplayName("POST /api/suggestions twice with same title increments timesSuggested")
    void suggest_twice_incrementsTimesSuggested() throws Exception {
        AcqSugRequestDTO req = new AcqSugRequestDTO();
        req.setTitle("Domain-Driven Design");
        req.setGenre("Programming");
        req.setAuthorFirstName("Eric");
        req.setAuthorLastName("Evans");

        // First suggestion -> timesSuggested = 1
        mockMvc.perform(post("/api/suggestions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.timesSuggested", is(1)));

        // Second suggestion with same title -> timesSuggested = 2
        mockMvc.perform(post("/api/suggestions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("Domain-Driven Design")))
                .andExpect(jsonPath("$.timesSuggested", is(2)));
    }
}
