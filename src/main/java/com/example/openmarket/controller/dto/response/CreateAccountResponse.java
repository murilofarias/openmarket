package com.example.openmarket.controller.dto.response;

import java.util.UUID;

public class CreateAccountResponse {

    private UUID id;

    public CreateAccountResponse() {}

    public CreateAccountResponse(UUID id) {
        this.id = id;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
}
