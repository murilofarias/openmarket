package com.example.openmarket.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateSellerRequest {

    @NotBlank(message = "Store name is required")
    @Size(min = 3, max = 100, message = "Store name must be between 3 and 100 characters")
    private String storeName;

    @Size(max = 500, message = "Store description must not exceed 500 characters")
    private String storeDescription;

    public CreateSellerRequest() {}

    public CreateSellerRequest(String storeName, String storeDescription) {
        this.storeName = storeName;
        this.storeDescription = storeDescription;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getStoreDescription() {
        return storeDescription;
    }

    public void setStoreDescription(String storeDescription) {
        this.storeDescription = storeDescription;
    }
}
