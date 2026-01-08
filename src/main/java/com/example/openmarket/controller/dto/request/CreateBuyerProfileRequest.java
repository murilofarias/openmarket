package com.example.openmarket.controller.dto.request;

public class CreateBuyerProfileRequest {

    private String defaultShippingAddress;

    public CreateBuyerProfileRequest() {}

    public CreateBuyerProfileRequest(String defaultShippingAddress) {
        this.defaultShippingAddress = defaultShippingAddress;
    }

    public String getDefaultShippingAddress() {
        return defaultShippingAddress;
    }

    public void setDefaultShippingAddress(String defaultShippingAddress) {
        this.defaultShippingAddress = defaultShippingAddress;
    }
}
