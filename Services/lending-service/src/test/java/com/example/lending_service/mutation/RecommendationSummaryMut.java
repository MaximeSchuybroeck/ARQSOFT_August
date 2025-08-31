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
import static org.assertj.core.api.AssertionsForClassTypes.within;
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

    // Mutation+: all positive -> ratio == 1.0 (kills “<” vs “<=” and wrong denominator mutants)
    @Test
    void summary_allPositive_ratioIsOne() {
        Long bookId = 21L;
        when(recRepo.countByBookIdAndSentiment(bookId, Sentiment.POSITIVE)).thenReturn(5L);
        when(recRepo.countByBookIdAndSentiment(bookId, Sentiment.NEGATIVE)).thenReturn(0L);
        when(recRepo.countByBookId(bookId)).thenReturn(5L);

        var dto = service.getRecommendationSummary(bookId);

        assertThat(dto.getPositives()).isEqualTo(5);
        assertThat(dto.getNegatives()).isEqualTo(0);
        assertThat(dto.getTotal()).isEqualTo(5);
        assertThat(dto.getPositiveRatio()).isEqualTo(1.0d);
    }

    // Mutation+: only negatives -> ratio == 0.0 (kills swapped numerator mutants)
    @Test
    void summary_onlyNegatives_ratioZero() {
        Long bookId = 22L;
        when(recRepo.countByBookIdAndSentiment(bookId, Sentiment.POSITIVE)).thenReturn(0L);
        when(recRepo.countByBookIdAndSentiment(bookId, Sentiment.NEGATIVE)).thenReturn(7L);
        when(recRepo.countByBookId(bookId)).thenReturn(7L);

        var dto = service.getRecommendationSummary(bookId);

        assertThat(dto.getPositives()).isEqualTo(0);
        assertThat(dto.getNegatives()).isEqualTo(7);
        assertThat(dto.getTotal()).isEqualTo(7);
        assertThat(dto.getPositiveRatio()).isEqualTo(0.0d);
    }

    // Mutation+: non-terminating decimal -> ensure floating division (kills integer-division mutants)
    @Test
    void summary_fractionalRatio_twoOfThree_isTwoThirds() {
        Long bookId = 23L;
        when(recRepo.countByBookIdAndSentiment(bookId, Sentiment.POSITIVE)).thenReturn(2L);
        when(recRepo.countByBookIdAndSentiment(bookId, Sentiment.NEGATIVE)).thenReturn(1L);
        when(recRepo.countByBookId(bookId)).thenReturn(3L);

        var dto = service.getRecommendationSummary(bookId);

        assertThat(dto.getPositives()).isEqualTo(2);
        assertThat(dto.getNegatives()).isEqualTo(1);
        assertThat(dto.getTotal()).isEqualTo(3);
        assertThat(dto.getPositiveRatio()).isCloseTo(2.0 / 3.0, within(1.0e-9));
    }
}

