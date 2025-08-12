package com.example.lending_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

public class Client {
    @FeignClient(name = "book-service", url = "http://localhost:8083")
    public interface BookClient {
        @GetMapping("/api/books/{id}/exist")
        Boolean bookExists(@PathVariable Long id);
    }

    @FeignClient(name = "reader-service")
    public interface ReaderClient {
        @GetMapping("/api/readers/email/exist")
        Boolean readerExists(@RequestParam("value") String email);
    }

}
