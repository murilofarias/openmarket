package com.example.openmarket.controller.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public class BuyerProfileDto {
    private final UUID id;
    private final String email;  // From JWT
    private final String name;   // From JWT
    private final String defaultShippingAddress;
    private final Integer totalOrders;
    private final Double rating;
    private final LocalDateTime createdAt;

    public BuyerProfileDto(UUID id, String email, String name,
                          String defaultShippingAddress, Integer totalOrders,
                          Double rating, LocalDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.defaultShippingAddress = defaultShippingAddress;
        this.totalOrders = totalOrders;
        this.rating = rating;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public String getName() { return name; }
    public String getDefaultShippingAddress() { return defaultShippingAddress; }
    public Integer getTotalOrders() { return totalOrders; }
    public Double getRating() { return rating; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
