package com.example.openmarket.controller.dto.response;

import java.util.UUID;

public class CreateReviewResponse {

    private UUID id;

    public CreateReviewResponse() {}

    public CreateReviewResponse(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}
