package com.example.lending_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

public class Client {

    @FeignClient(name = "book-service", url = "${book.service.url:http://book-service:8083}")
    public interface BookClient {
        @GetMapping("/api/books/{id}/exist")
        Boolean bookExists(@PathVariable("id") Long id);
    }

    @FeignClient(name = "reader-service", url = "${reader.service.url:http://reader-service:8085}") // <— NOT localhost
    public interface ReaderClient {
        @GetMapping("/api/readers/email/exist")
        Boolean readerExists(@RequestParam("value") String email);
    }
}
