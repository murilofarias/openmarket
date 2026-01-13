package com.example.openmarket.infrastructure.persistence.entity;

import com.example.openmarket.application.domain.SellerStatus;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "seller_profiles", indexes = {
    @Index(name = "idx_seller_user_id", columnList = "user_id", unique = true),
    @Index(name = "idx_seller_status", columnList = "status")
})
public class SellerProfileEntity extends BaseEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private String userId;  // Keycloak subject

    @Column(name = "store_name", nullable = false)
    private String storeName;

    @Column(name = "store_description", columnDefinition = "TEXT")
    private String storeDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SellerStatus status;

    @Column(name = "rating")
    private Double rating;

    public SellerProfileEntity() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }

    public String getStoreDescription() { return storeDescription; }
    public void setStoreDescription(String storeDescription) {
        this.storeDescription = storeDescription;
    }

    public SellerStatus getStatus() { return status; }
    public void setStatus(SellerStatus status) { this.status = status; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }
}
