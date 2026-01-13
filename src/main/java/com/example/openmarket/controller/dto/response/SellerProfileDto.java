package com.example.openmarket.controller.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public class SellerProfileDto {
    private final UUID id;
    private final String email;  // From JWT
    private final String name;   // From JWT
    private final String storeName;
    private final String storeDescription;
    private final String status;
    private final Double rating;
    private final LocalDateTime createdAt;

    public SellerProfileDto(UUID id, String email, String name,
                           String storeName, String storeDescription,
                           String status, Double rating, LocalDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.storeName = storeName;
        this.storeDescription = storeDescription;
        this.status = status;
        this.rating = rating;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public String getName() { return name; }
    public String getStoreName() { return storeName; }
    public String getStoreDescription() { return storeDescription; }
    public String getStatus() { return status; }
    public Double getRating() { return rating; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
