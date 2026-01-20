package com.example.openmarket.application.domain;

import com.example.openmarket.application.exception.DomainException;

import java.time.LocalDateTime;
import java.util.UUID;

public class BuyerProfile {

    private final UUID id;
    private final String userId;  // Keycloak subject (from JWT)
    private String defaultShippingAddress;
    private Integer totalOrders;
    private Double rating;
    private final LocalDateTime createdAt;

    private BuyerProfile(UUID id, String userId, String defaultShippingAddress,
                        Integer totalOrders, Double rating, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.defaultShippingAddress = defaultShippingAddress;
        this.totalOrders = totalOrders;
        this.rating = rating;
        this.createdAt = createdAt;
    }

    public static BuyerProfile create(String userId, String defaultShippingAddress) {
        validateUserId(userId);
        return new BuyerProfile(
            UUID.randomUUID(),
            userId,
            defaultShippingAddress,
            0,
            null,
            null  // Will be set by JPA
        );
    }

    public static BuyerProfile reconstitute(UUID id, String userId, String defaultShippingAddress,
                                           Integer totalOrders, Double rating, LocalDateTime createdAt) {
        return new BuyerProfile(id, userId, defaultShippingAddress, totalOrders, rating, createdAt);
    }

    // Business logic
    public void recordOrder() {
        this.totalOrders++;
    }

    public void updateShippingAddress(String newAddress) {
        if (newAddress == null || newAddress.trim().isEmpty()) {
            throw new DomainException("Shipping address cannot be empty");
        }
        this.defaultShippingAddress = newAddress;
    }

    public void updateRating(double newRating) {
        if (newRating < 0 || newRating > 5) {
            throw new DomainException("Rating must be between 0 and 5");
        }
        this.rating = newRating;
    }

    // Validation
    private static void validateUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new DomainException("User ID cannot be empty");
        }
    }

    // Getters
    public UUID getId() { return id; }
    public String getUserId() { return userId; }
    public String getDefaultShippingAddress() { return defaultShippingAddress; }
    public Integer getTotalOrders() { return totalOrders; }
    public Double getRating() { return rating; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
