package com.example.openmarket.controller.dto.response;

public class RegisterUserResponse {
    private final String userId;
    private final String message;

    public RegisterUserResponse(String userId, String message) {
        this.userId = userId;
        this.message = message;
    }

    public String getUserId() { return userId; }
    public String getMessage() { return message; }
}
