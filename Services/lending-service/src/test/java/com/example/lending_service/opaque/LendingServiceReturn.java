package com.example.lending_service.opaque;

import com.example.lending_service.client.Client;
import com.example.lending_service.entity.Lending;
import com.example.lending_service.entity.Recommendation;
import com.example.lending_service.entity.Recommendation.Sentiment;
import com.example.lending_service.repository.LendingRepository;
import com.example.lending_service.repository.RecommendationRepository;
import com.example.lending_service.service.LendingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
public class LendingServiceReturn {

    @Mock LendingRepository repo;
    @Mock RecommendationRepository recRepo;
    @Mock Client.BookClient bookClient;
    @Mock Client.ReaderClient readerClient;
    @Mock RabbitTemplate rabbitTemplate;

    @InjectMocks
    LendingService service;

    private Lending newActiveLending(Long id, Long bookId, String email) {
        Lending l = new Lending();
        l.setId(id);
        l.setBookId(bookId);
        l.setReaderEmail(email);
        l.setStartDate(LocalDate.now());
        l.setDueDate(LocalDate.now().plusDays(7));
        l.setReturnedDate(null);
        return l;
    }

    // Opaque #1: returning with POSITIVE recommendation saves recommendation and sets returnedDate
    @Test
    void returnById_withPositiveRecommendation_persistsRecAndClosesLending() {
        Lending l = newActiveLending(42L, 1L, "alice@example.com");
        when(repo.findByIdAndReturnedDateIsNull(42L)).thenReturn(Optional.of(l));
        when(recRepo.existsByLendingId(42L)).thenReturn(false);

        LendingService.LendingDTO dto = service.returnById(42L, true, "Great read!");

        assertThat(dto.getReturnedDate()).isNotNull();
        verify(repo).save(any(Lending.class));
        verify(recRepo).save(argThat(r ->
                r.getBookId().equals(1L) &&
                        r.getLendingId().equals(42L) &&
                        r.getSentiment() == Sentiment.POSITIVE &&
                        "Great read!".equals(r.getComment())
        ));
    }

    // Opaque #2: returning without recommendation must NOT create a Recommendation row
    @Test
    void returnById_withoutRecommendation_doesNotCreateRecommendation() {
        Lending l = newActiveLending(13L, 5L, "bob@example.com");
        when(repo.findByIdAndReturnedDateIsNull(13L)).thenReturn(Optional.of(l));

        service.returnById(13L, null, null);

        verify(repo).save(any(Lending.class));
        verify(recRepo, never()).save(any(Recommendation.class));
    }

    // Opaque+: returning with NEGATIVE recommendation -> persist recommendation and close lending
    @Test
    void returnById_withNegativeRecommendation_persistsRecAndClosesLending() {
        Lending l = newActiveLending(100L, 55L, "lea@example.com");
        when(repo.findByIdAndReturnedDateIsNull(100L)).thenReturn(Optional.of(l));
        when(recRepo.existsByLendingId(100L)).thenReturn(false);

        service.returnById(100L, false, "did not enjoy"); // false -> NEGATIVE

        verify(repo, times(1)).save(any(Lending.class));
        verify(recRepo, times(1)).save(any(Recommendation.class));
    }

    // Opaque+: duplicate recommendation on the same lending -> throw and do not save
    @Test
    void returnById_duplicateRecommendation_throws_butStillClosesLending_andDoesNotPersistRecommendation() {
        Lending l = newActiveLending(101L, 56L, "sam@example.com");
        when(repo.findByIdAndReturnedDateIsNull(101L)).thenReturn(Optional.of(l));
        when(recRepo.existsByLendingId(101L)).thenReturn(true); // already recommended

        assertThatThrownBy(() -> service.returnById(101L, true, "great"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("recommendation"); // keep message flexible

        // The service saves the lending (marks it returned) before discovering the duplicate
        verify(repo, times(1)).save(any(Lending.class));

        // But it must NOT save a new recommendation
        verify(recRepo, never()).save(any(Recommendation.class));

        // Verify the interaction order to make the spec explicit
        InOrder inOrder = inOrder(repo, recRepo);
        inOrder.verify(repo).save(any(Lending.class));               // close lending
        inOrder.verify(recRepo).existsByLendingId(101L);             // then check duplicate
        // no recRepo.save afterwards
        verifyNoMoreInteractions(recRepo);
        verifyNoInteractions(rabbitTemplate, bookClient, readerClient);
    }
}

