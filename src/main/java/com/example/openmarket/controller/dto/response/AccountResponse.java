package com.example.openmarket.controller.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public class AccountResponse {

    private UUID id;
    private String email;
    private String name;
    private BuyerProfileResponse buyerProfile;
    private SellerProfileResponse sellerProfile;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public AccountResponse() {}

    public AccountResponse(UUID id, String email, String name,
                          BuyerProfileResponse buyerProfile, SellerProfileResponse sellerProfile,
                          LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.buyerProfile = buyerProfile;
        this.sellerProfile = sellerProfile;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BuyerProfileResponse getBuyerProfile() { return buyerProfile; }
    public void setBuyerProfile(BuyerProfileResponse buyerProfile) { this.buyerProfile = buyerProfile; }

    public SellerProfileResponse getSellerProfile() { return sellerProfile; }
    public void setSellerProfile(SellerProfileResponse sellerProfile) { this.sellerProfile = sellerProfile; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
