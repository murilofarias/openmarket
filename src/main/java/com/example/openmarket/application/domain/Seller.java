package com.example.openmarket.application.domain;

import java.time.LocalDateTime;

public class Seller extends Profile {

    private SellerStatus sellerStatus;
    private String storeName;
    private String storeDescription;

    protected Seller(String storeName, String storeDescription) {
        super();
        this.storeName = storeName;
        this.storeDescription = storeDescription;
        this.sellerStatus = SellerStatus.PENDING_VERIFICATION;
    }

    protected Seller(LocalDateTime createdAt, Double rating, String storeName,
                     String storeDescription, SellerStatus sellerStatus) {
        super(createdAt, rating);
        this.storeName = storeName;
        this.storeDescription = storeDescription;
        this.sellerStatus = sellerStatus;
    }

    public static Seller create(String storeName, String storeDescription) {
        return new Seller(storeName, storeDescription);
    }

    public static Seller reconstitute(LocalDateTime createdAt, Double rating, String storeName,
                                      String storeDescription, SellerStatus sellerStatus) {
        return new Seller(createdAt, rating, storeName, storeDescription, sellerStatus);
    }

    public SellerStatus getSellerStatus() {
        return sellerStatus;
    }

    public String getStoreName() {
        return storeName;
    }

    public String getStoreDescription() { return storeDescription;}

    @Override
    public Role getProfileRole() {
        return Role.SELLER;
    }
}
