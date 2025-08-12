package com.example.lending_service.service;

import com.example.lending_service.entity.Lending;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class LendingEventConsumer {

    @RabbitListener(queues = "book.lent.queue")
    public void handleBookLent(Lending lending) {
        // Store analytics info (e.g., increment genre count, save timestamp, etc.)
        System.out.println("Received BookLent: " + lending);
    }

    @RabbitListener(queues = "book.returned.queue")
    public void handleBookReturned(Lending lending) {
        // Update duration, stats, reports, etc.
        System.out.println("Received BookReturned: " + lending);
    }
}