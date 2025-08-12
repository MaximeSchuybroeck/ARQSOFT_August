package com.example.lending_service.controller;

import com.example.lending_service.dto.LendingDTO;
import com.example.lending_service.dto.LendingResponse;
import com.example.lending_service.service.LendingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lendings")
public class LendingController {

    @Autowired
    private LendingService service;

    @PostMapping("/borrow")
    public ResponseEntity<LendingResponse> borrow(@RequestBody LendingDTO dto) {
        LendingResponse response = service.lendBook(dto);
        return ResponseEntity.ok(response);
    }
    @GetMapping("all")
    public ResponseEntity<List<LendingDTO>> getAllLendings() {
        List<LendingDTO> lendings = service.getAllLendings();
        return ResponseEntity.ok(lendings);
    }
    @PostMapping("/return/{id}")
    public ResponseEntity<LendingDTO> returnBook(@PathVariable Long id) {
        LendingDTO result = service.returnBook(id);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<LendingDTO>> getOverdue() {
        List<LendingDTO> result = service.getOverdues();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/average-duration")
    public ResponseEntity<Double> averageLendingDuration() {
        Double result = service.getAverageLendingDuration();
        return ResponseEntity.ok(result);
    }
}
