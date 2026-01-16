package com.example.openmarket.controller.dto.response;

import java.util.UUID;

public class CreateProductResponse {

    private UUID id;

    public CreateProductResponse() {}

    public CreateProductResponse(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}
