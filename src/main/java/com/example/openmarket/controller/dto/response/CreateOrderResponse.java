package com.example.openmarket.controller.dto.response;

import java.util.UUID;

public class CreateOrderResponse {

    private UUID orderId;

    public CreateOrderResponse() {}

    public CreateOrderResponse(UUID orderId) {
        this.orderId = orderId;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }
}
