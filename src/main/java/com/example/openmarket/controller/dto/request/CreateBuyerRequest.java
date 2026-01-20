package com.example.openmarket.controller.dto.request;

import jakarta.validation.constraints.NotBlank;

public class CreateBuyerRequest {

    @NotBlank(message = "Shipping address is required")
    private String defaultShippingAddress;

    public CreateBuyerRequest() {}

    public CreateBuyerRequest(String defaultShippingAddress) {
        this.defaultShippingAddress = defaultShippingAddress;
    }

    public String getDefaultShippingAddress() {
        return defaultShippingAddress;
    }

    public void setDefaultShippingAddress(String defaultShippingAddress) {
        this.defaultShippingAddress = defaultShippingAddress;
    }
}
