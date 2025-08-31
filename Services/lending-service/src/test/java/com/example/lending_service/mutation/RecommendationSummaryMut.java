package com.example.lending_service.mutation;

import com.example.lending_service.client.Client;
import com.example.lending_service.repository.LendingRepository;
import com.example.lending_service.repository.RecommendationRepository;
import com.example.lending_service.entity.Recommendation.Sentiment;
import com.example.lending_service.service.LendingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
public class RecommendationSummaryMut {

    @Mock LendingRepository repo;
    @Mock RecommendationRepository recRepo;
    @Mock Client.BookClient bookClient;
    @Mock Client.ReaderClient readerClient;
    @Mock RabbitTemplate rabbitTemplate;

    @InjectMocks
    LendingService service;

    // Mutation #1: zero-total branch must keep ratio == 0 (kills divide-by-zero and boundary mutants)
    @Test
    void summary_whenNoRecommendations_ratioIsZero() {
        Long bookId = 10L;
        when(recRepo.countByBookIdAndSentiment(bookId, Sentiment.POSITIVE)).thenReturn(0L);
        when(recRepo.countByBookIdAndSentiment(bookId, Sentiment.NEGATIVE)).thenReturn(0L);
        when(recRepo.countByBookId(bookId)).thenReturn(0L);

        var dto = service.getRecommendationSummary(bookId);

        assertThat(dto.getTotal()).isEqualTo(0);
        assertThat(dto.getPositiveRatio()).isEqualTo(0.0d);
    }

    // Mutation #2: normal case ratio (kills off-by-one/branch mutants)
    @Test
    void summary_withPositivesAndNegatives_ratioComputed() {
        Long bookId = 11L;
        when(recRepo.countByBookIdAndSentiment(bookId, Sentiment.POSITIVE)).thenReturn(2L);
        when(recRepo.countByBookIdAndSentiment(bookId, Sentiment.NEGATIVE)).thenReturn(2L);
        when(recRepo.countByBookId(bookId)).thenReturn(4L);

        var dto = service.getRecommendationSummary(bookId);

        assertThat(dto.getPositives()).isEqualTo(2);
        assertThat(dto.getNegatives()).isEqualTo(2);
        assertThat(dto.getTotal()).isEqualTo(4);
        assertThat(dto.getPositiveRatio()).isEqualTo(0.5d);
    }
}
