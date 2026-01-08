package com.example.openmarket.controller.dto.response;

import java.time.LocalDateTime;

public class SellerProfileResponse {

    private String sellerStatus;
    private String storeName;
    private String storeDescription;
    private Double rating;
    private LocalDateTime createdAt;

    public SellerProfileResponse() {}

    public SellerProfileResponse(String sellerStatus, String storeName, String storeDescription,
                                Double rating, LocalDateTime createdAt) {
        this.sellerStatus = sellerStatus;
        this.storeName = storeName;
        this.storeDescription = storeDescription;
        this.rating = rating;
        this.createdAt = createdAt;
    }

    public String getSellerStatus() { return sellerStatus; }
    public void setSellerStatus(String sellerStatus) { this.sellerStatus = sellerStatus; }

    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }

    public String getStoreDescription() { return storeDescription; }
    public void setStoreDescription(String storeDescription) { this.storeDescription = storeDescription; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
