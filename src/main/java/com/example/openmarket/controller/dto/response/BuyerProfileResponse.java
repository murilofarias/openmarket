package com.example.openmarket.controller.dto.response;

import java.time.LocalDateTime;

public class BuyerProfileResponse {

    private String defaultShippingAddress;
    private Integer totalOrders;
    private Double rating;
    private LocalDateTime createdAt;

    public BuyerProfileResponse() {}

    public BuyerProfileResponse(String defaultShippingAddress, Integer totalOrders,
                               Double rating, LocalDateTime createdAt) {
        this.defaultShippingAddress = defaultShippingAddress;
        this.totalOrders = totalOrders;
        this.rating = rating;
        this.createdAt = createdAt;
    }

    public String getDefaultShippingAddress() { return defaultShippingAddress; }
    public void setDefaultShippingAddress(String defaultShippingAddress) {
        this.defaultShippingAddress = defaultShippingAddress;
    }

    public Integer getTotalOrders() { return totalOrders; }
    public void setTotalOrders(Integer totalOrders) { this.totalOrders = totalOrders; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
