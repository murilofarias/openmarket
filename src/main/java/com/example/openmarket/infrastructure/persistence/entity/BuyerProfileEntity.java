package com.example.openmarket.infrastructure.persistence.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "buyer_profiles", indexes = {
    @Index(name = "idx_buyer_user_id", columnList = "user_id", unique = true)
})
public class BuyerProfileEntity extends BaseEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private String userId;  // Keycloak subject

    @Column(name = "default_shipping_address")
    private String defaultShippingAddress;

    @Column(name = "total_orders", nullable = false)
    private Integer totalOrders;

    @Column(name = "rating")
    private Double rating;

    public BuyerProfileEntity() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getDefaultShippingAddress() { return defaultShippingAddress; }
    public void setDefaultShippingAddress(String defaultShippingAddress) {
        this.defaultShippingAddress = defaultShippingAddress;
    }

    public Integer getTotalOrders() { return totalOrders; }
    public void setTotalOrders(Integer totalOrders) { this.totalOrders = totalOrders; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }
}
