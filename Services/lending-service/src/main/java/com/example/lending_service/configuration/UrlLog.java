package com.example.lending_service.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class UrlLog {
    public UrlLog(
            @Value("${book.service.url}") String bookUrl,
            @Value("${reader.service.url}") String readerUrl
    ) {
        log.info("book.service.url   = {}", bookUrl);
        log.info("reader.service.url = {}", readerUrl);
    }
}
