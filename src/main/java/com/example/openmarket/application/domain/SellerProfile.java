package com.example.openmarket.application.domain;

import com.example.openmarket.application.exception.DomainException;

import java.time.LocalDateTime;
import java.util.UUID;

public class SellerProfile {

    private final UUID id;
    private final String userId;  // Keycloak subject (from JWT)
    private String storeName;
    private String storeDescription;
    private SellerStatus status;
    private Double rating;
    private final LocalDateTime createdAt;

    private SellerProfile(UUID id, String userId, String storeName, String storeDescription,
                         SellerStatus status, Double rating, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.storeName = storeName;
        this.storeDescription = storeDescription;
        this.status = status;
        this.rating = rating;
        this.createdAt = createdAt;
    }

    public static SellerProfile create(String userId, String storeName, String storeDescription) {
        validateUserId(userId);
        validateStoreName(storeName);
        return new SellerProfile(
            UUID.randomUUID(),
            userId,
            storeName,
            storeDescription,
            SellerStatus.PENDING_VERIFICATION,
            null,
            null  // Will be set by JPA
        );
    }

    public static SellerProfile reconstitute(UUID id, String userId, String storeName,
                                            String storeDescription, SellerStatus status,
                                            Double rating, LocalDateTime createdAt) {
        return new SellerProfile(id, userId, storeName, storeDescription, status, rating, createdAt);
    }

    // Business logic
    public void approve() {
        if (this.status != SellerStatus.PENDING_VERIFICATION) {
            throw new DomainException("Can only approve sellers pending verification");
        }
        this.status = SellerStatus.ACTIVE;
    }

    public void suspend(String reason) {
        if (this.status != SellerStatus.ACTIVE) {
            throw new DomainException("Can only suspend active sellers");
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new DomainException("Suspension reason is required");
        }
        this.status = SellerStatus.SUSPENDED;
    }

    public void reactivate() {
        if (this.status != SellerStatus.SUSPENDED) {
            throw new DomainException("Can only reactivate suspended sellers");
        }
        this.status = SellerStatus.ACTIVE;
    }

    public void updateStoreInfo(String storeName, String storeDescription) {
        validateStoreName(storeName);
        this.storeName = storeName;
        this.storeDescription = storeDescription;
    }

    public void updateRating(double newRating) {
        if (newRating < 0 || newRating > 5) {
            throw new DomainException("Rating must be between 0 and 5");
        }
        this.rating = newRating;
    }

    public boolean isActive() {
        return this.status == SellerStatus.ACTIVE;
    }

    // Validation
    private static void validateUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new DomainException("User ID cannot be empty");
        }
    }

    private static void validateStoreName(String storeName) {
        if (storeName == null || storeName.trim().isEmpty()) {
            throw new DomainException("Store name cannot be empty");
        }
        if (storeName.length() < 3) {
            throw new DomainException("Store name must be at least 3 characters");
        }
    }

    // Getters
    public UUID getId() { return id; }
    public String getUserId() { return userId; }
    public String getStoreName() { return storeName; }
    public String getStoreDescription() { return storeDescription; }
    public SellerStatus getStatus() { return status; }
    public Double getRating() { return rating; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
