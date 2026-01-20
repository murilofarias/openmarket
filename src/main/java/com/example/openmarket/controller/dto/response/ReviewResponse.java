package com.example.openmarket.controller.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public class ReviewResponse {

    private UUID id;
    private UUID productId;
    private UUID buyerProfileId;
    private String buyerName;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;

    public ReviewResponse() {}

    public ReviewResponse(UUID id, UUID productId, UUID buyerProfileId, String buyerName,
                         Integer rating, String comment, LocalDateTime createdAt) {
        this.id = id;
        this.productId = productId;
        this.buyerProfileId = buyerProfileId;
        this.buyerName = buyerName;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public UUID getBuyerProfileId() {
        return buyerProfileId;
    }

    public void setBuyerProfileId(UUID buyerProfileId) {
        this.buyerProfileId = buyerProfileId;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
