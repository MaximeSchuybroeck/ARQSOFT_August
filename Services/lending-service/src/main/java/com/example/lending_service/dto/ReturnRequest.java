// dto/ReturnRequest.java
package com.example.lending_service.dto;

import lombok.Data;

@Data
public class ReturnRequest {
    private String readerEmail;
    private Long bookId;
}
