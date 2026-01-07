package com.example.openmarket.application.domain;

import java.time.LocalDateTime;

public class Buyer extends Profile {

    private String defaultShippingAddress;
    private Integer totalOrders;

    private Buyer(String defaultShippingAddress) {
        super();
        this.defaultShippingAddress = defaultShippingAddress;
        this.totalOrders = 0;
    }

    private Buyer(LocalDateTime createdAt, Double rating, String defaultShippingAddress, Integer totalOrders) {
        super(createdAt, rating);
        this.defaultShippingAddress = defaultShippingAddress;
        this.totalOrders = totalOrders;
    }

    public static Buyer create(String defaultShippingAddress) {
        return new Buyer(defaultShippingAddress);
    }

    public static Buyer reconstitute(LocalDateTime createdAt, Double rating, String defaultShippingAddress,
                                     Integer totalOrders) {
        return new Buyer(createdAt, rating, defaultShippingAddress, totalOrders);
    }

    // Business logic
    public void recordOrder() {
        this.totalOrders++;
    }

    // Getters
    public String getDefaultShippingAddress() { return defaultShippingAddress; }
    public void setDefaultShippingAddress(String address) { this.defaultShippingAddress = address; }
    public Integer getTotalOrders() { return totalOrders; }

    @Override
    public Role getProfileRole() {
        return Role.BUYER;
    }
}
