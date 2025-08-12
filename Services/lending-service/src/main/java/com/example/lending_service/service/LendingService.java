package com.example.lending_service.service;

import com.example.lending_service.client.Client;
import com.example.lending_service.dto.LendingDTO;
import com.example.lending_service.dto.LendingResponse;
import com.example.lending_service.entity.Lending;
import com.example.lending_service.repository.LendingRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LendingService {

        private static final int DURATION_DAYS = 14;

        @Autowired
        private LendingRepository repo;

        @Autowired
        private RabbitTemplate rabbitTemplate;

        @Autowired
        private Client.BookClient bookClient;

        @Autowired
        private Client.ReaderClient readerClient;

    public LendingResponse lendBook(LendingDTO dto) {
        if (!bookClient.bookExists(dto.getBookId())) {
            return new LendingResponse(false, "Book with ID " + dto.getBookId() + " does not exist.");
        }

        if (!readerClient.readerExists(dto.getReaderEmail())) {
            return new LendingResponse(false, "Reader with email " + dto.getReaderEmail() + " does not exist.");
        }

        Optional<Lending> existing = repo.findByBookIdAndReturnedDateIsNull(dto.getBookId());
        if (existing.isPresent()) {
            return new LendingResponse(false, "Book with ID " + dto.getBookId() + " is currently lent out.");
        }

        Lending lending = new Lending();
        lending.setBookId(dto.getBookId());
        lending.setReaderEmail(dto.getReaderEmail());
        lending.setStartDate(LocalDate.now());
        lending.setDueDate(LocalDate.now().plusDays(DURATION_DAYS));

        Lending saved = repo.save(lending);
        rabbitTemplate.convertAndSend("lending.exchange", "lending.book-lent", saved);

        LendingDTO responseDTO = new LendingDTO();
        responseDTO.setBookId(saved.getBookId());
        responseDTO.setReaderEmail(saved.getReaderEmail());
        responseDTO.setStartDate(saved.getStartDate());
        responseDTO.setDueDate(saved.getDueDate());

        return new LendingResponse(true, "Book successfully lent.", responseDTO);
    }
    public List<LendingDTO> getAllLendings() {
        return repo.findAll()
                .stream()
                .map(LendingDTO::new)
                .toList();
    }

    public LendingDTO returnBook(Long id) {
        Lending l = repo.findById(id).orElseThrow();
        l.setReturnedDate(LocalDate.now());
        Lending updated = repo.save(l);

        rabbitTemplate.convertAndSend("lending.exchange", "lending.book-returned", updated); // Event

        return toDTO(updated);
    }

    public List<LendingDTO> getOverdues() {
        return repo.findOverdueLendings().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public Double getAverageLendingDuration() {
        return repo.averageLendingDuration();
    }

    private LendingDTO toDTO(Lending l) {
        LendingDTO dto = new LendingDTO();
        dto.setId(l.getId());
        dto.setReaderEmail(l.getReaderEmail());
        dto.setBookId(l.getBookId());
        dto.setStartDate(l.getStartDate());
        dto.setDueDate(l.getDueDate());
        dto.setReturnedDate(l.getReturnedDate());
        return dto;
    }
}
