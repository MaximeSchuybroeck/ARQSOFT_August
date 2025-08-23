package com.example.lending_service.transparent;

import com.example.lending_service.client.Client;
import com.example.lending_service.entity.Lending;
import com.example.lending_service.entity.Recommendation;
import com.example.lending_service.repository.LendingRepository;
import com.example.lending_service.repository.RecommendationRepository;
import com.example.lending_service.service.LendingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class LendingServiceReturn {

    @Mock LendingRepository repo;
    @Mock RecommendationRepository recRepo;
    @Mock Client.BookClient bookClient;
    @Mock Client.ReaderClient readerClient;
    @Mock RabbitTemplate rabbitTemplate;

    @InjectMocks
    LendingService service;

    private Lending active(Long id) {
        Lending l = new Lending();
        l.setId(id);
        l.setBookId(1L);
        l.setReaderEmail("x@y.com");
        l.setStartDate(LocalDate.now());
        l.setDueDate(LocalDate.now().plusDays(3));
        return l;
    }

    // Transparent #1: no active lending -> IllegalArgumentException
    @Test
    void returnById_whenNotActive_throws() {
        when(repo.findByIdAndReturnedDateIsNull(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.returnById(999L, true, "X"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Lending 999 not found.");
    }

    // Transparent #2: duplicate recommendation on same lending -> IllegalStateException
    @Test
    void returnById_duplicateRecommendation_throwsConflict() {
        Lending l = active(7L);
        when(repo.findByIdAndReturnedDateIsNull(7L)).thenReturn(Optional.of(l));
        when(recRepo.existsByLendingId(7L)).thenReturn(true); // already recommended on this lending

        assertThatThrownBy(() -> service.returnById(7L, false, "meh"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("recommendation already exists");

        // ensure we didn't save anything new
        verify(recRepo, never()).save(any(Recommendation.class));
    }
}
