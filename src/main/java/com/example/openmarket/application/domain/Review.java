package com.example.openmarket.application.domain;

import java.time.LocalDateTime;
import java.util.UUID;

public class Review {

    private UUID id;
    private final UUID productId;
    private final UUID buyerProfileId;
    private final String buyerName;
    private final UUID orderId;
    private Integer rating;
    private String comment;
    private final LocalDateTime createdAt;

    private Review(UUID id, UUID productId, UUID buyerProfileId, String buyerName,
                   UUID orderId, Integer rating, String comment, LocalDateTime createdAt) {
        this.id = id;
        this.productId = productId;
        this.buyerProfileId = buyerProfileId;
        this.buyerName = buyerName;
        this.orderId = orderId;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    public static Review create(UUID productId, UUID buyerProfileId, String buyerName,
                                UUID orderId, Integer rating, String comment) {
        validateRating(rating);
        validateComment(comment);
        validateBuyerName(buyerName);
        return new Review(UUID.randomUUID(), productId, buyerProfileId, buyerName,
                         orderId, rating, comment, null);
    }

    public static Review reconstitute(UUID id, UUID productId, UUID buyerProfileId,
                                     String buyerName, UUID orderId, Integer rating,
                                     String comment, LocalDateTime createdAt) {
        return new Review(id, productId, buyerProfileId, buyerName, orderId, rating,
                         comment, createdAt);
    }

    public void updateRating(Integer newRating) {
        validateRating(newRating);
        this.rating = newRating;
    }

    public void updateComment(String newComment) {
        validateComment(newComment);
        this.comment = newComment;
    }

    // Validation
    private static void validateRating(Integer rating) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
    }

    private static void validateComment(String comment) {
        if (comment != null && comment.length() > 1000) {
            throw new IllegalArgumentException("Comment must not exceed 1000 characters");
        }
    }

    private static void validateBuyerName(String buyerName) {
        if (buyerName == null || buyerName.trim().isEmpty()) {
            throw new IllegalArgumentException("Buyer name cannot be empty");
        }
        if (buyerName.length() > 100) {
            throw new IllegalArgumentException("Buyer name must not exceed 100 characters");
        }
    }

    // Getters
    public UUID getId() { return id; }
    public UUID getProductId() { return productId; }
    public UUID getBuyerProfileId() { return buyerProfileId; }
    public String getBuyerName() { return buyerName; }
    public UUID getOrderId() { return orderId; }
    public Integer getRating() { return rating; }
    public String getComment() { return comment; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
