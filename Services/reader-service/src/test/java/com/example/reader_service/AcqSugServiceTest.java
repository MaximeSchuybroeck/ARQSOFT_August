package com.example.reader_service;

import com.example.reader_service.dto.AcqSugRequestDTO;
import com.example.reader_service.dto.AcqSugResponseDTO;
import com.example.reader_service.entity.AcquisitionSuggestion;
import com.example.reader_service.entity.AcquisitionSuggestion.Status;
import com.example.reader_service.repository.AcqSugRepository;
import com.example.reader_service.repository.ReaderRepository;
import com.example.reader_service.service.AcqSugService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AcqSugServiceTest {

    @Mock
    AcqSugRepository repository;

    // Present in service constructor; not used by these tests but required for wiring
    @Mock
    ReaderRepository readerRepository;

    @InjectMocks
    AcqSugService service;

    private static AcqSugRequestDTO request(
            String title,
            String genre,
            String authorFirst,
            String authorLast
    ) {
        AcqSugRequestDTO dto = new AcqSugRequestDTO();
        dto.setTitle(title);
        dto.setGenre(genre);
        dto.setAuthorFirstName(authorFirst);
        dto.setAuthorLastName(authorLast);
        return dto;
    }

    @Test
    @DisplayName("create(): creates a NEW suggestion when title does not exist")
    void create_newSuggestion_whenNotExists() {
        // Arrange
        // No existing suggestion with this title
        when(repository.findByTitle("Clean Code")).thenReturn(null);
        // Capture the entity that will be saved to assert default values
        ArgumentCaptor<AcquisitionSuggestion> captor = ArgumentCaptor.forClass(AcquisitionSuggestion.class);
        // Simulate DB assigning an ID
        when(repository.save(any(AcquisitionSuggestion.class))).thenAnswer(invocation -> {
            AcquisitionSuggestion s = invocation.getArgument(0);
            s.setId(1L);
            return s;
        });

        // Act
        AcqSugResponseDTO resp = service.create(request("Clean Code", "Programming", "Robert", "Martin"));

        // Assert
        verify(repository).save(captor.capture());
        AcquisitionSuggestion saved = captor.getValue();

        assertThat(saved.getTitle()).isEqualTo("Clean Code");
        assertThat(saved.getGenre()).isEqualTo("Programming");
        assertThat(saved.getAuthorFirstName()).isEqualTo("Robert");
        assertThat(saved.getAuthorLastName()).isEqualTo("Martin");
        assertThat(saved.getTimesSuggested()).isEqualTo(1); // default for new suggestion
        assertThat(saved.getStatus()).isEqualTo(Status.NEW); // default status

        assertThat(resp.getId()).isEqualTo(1L);
        assertThat(resp.getTitle()).isEqualTo("Clean Code");
        assertThat(resp.getStatus()).isEqualTo(Status.NEW);
        assertThat(resp.getTimesSuggested()).isEqualTo(1);
    }

    @Test
    @DisplayName("create(): increments timesSuggested when suggestion with same title exists")
    void create_incrementWhenExists() {
        // Arrange existing suggestion in DB
        AcquisitionSuggestion existing = new AcquisitionSuggestion();
        existing.setId(10L);
        existing.setTitle("Clean Code");
        existing.setGenre("Programming");
        existing.setAuthorFirstName("Robert");
        existing.setAuthorLastName("Martin");
        existing.setTimesSuggested(2);
        existing.setStatus(Status.NEW);

        when(repository.findByTitle("Clean Code")).thenReturn(existing);
        when(repository.save(any(AcquisitionSuggestion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        AcqSugResponseDTO resp = service.create(request("Clean Code", "Programming", "Robert", "Martin"));

        // Assert
        verify(repository).findByTitle("Clean Code");
        assertThat(existing.getTimesSuggested()).isEqualTo(3);
        assertThat(resp.getId()).isEqualTo(10L);
        assertThat(resp.getTimesSuggested()).isEqualTo(3);
        assertThat(resp.getStatus()).isEqualTo(Status.NEW);
    }
}
