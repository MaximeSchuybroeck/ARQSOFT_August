package com.example.lending_service.dto;

import lombok.Data;

@Data
public class LendingResponse {
    private boolean success;
    private String message;
    private LendingDTO data;

    public LendingResponse() {}

    public LendingResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public LendingResponse(boolean success, String message, LendingDTO data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LendingDTO getData() {
        return data;
    }

    public void setData(LendingDTO data) {
        this.data = data;
    }
}
