package com.example.openmarket.infrastructure.persistence.entity;

import com.example.openmarket.application.domain.SellerStatus;
import jakarta.persistence.*;

import java.util.Set;

@Entity
@Table(name = "sellers")
@DiscriminatorValue("SELLER")
public class SellerEntity extends ProfileEntity {

    @Column(name = "store_name", nullable = false)
    private String storeName;

    @Column(name = "store_description", columnDefinition = "TEXT")
    private String storeDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "seller_status")
    private SellerStatus sellerStatus;

    public SellerEntity() {}

    public String getStoreName() { return storeName; }
    public SellerStatus getSellerStatus() { return sellerStatus; }
    public void setSellerStatus(SellerStatus status) { this.sellerStatus = status; }

    public String getStoreDescription() {
        return storeDescription;
    }

    public void setStoreDescription(String storeDescription) {
        this.storeDescription = storeDescription;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }
}
