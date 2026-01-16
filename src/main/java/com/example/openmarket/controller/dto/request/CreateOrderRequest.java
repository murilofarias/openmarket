package com.example.openmarket.controller.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public class CreateOrderRequest {

    @NotNull(message = "Seller profile ID is required")
    private UUID sellerProfileId;

    @NotEmpty(message = "Order must contain at least one item")
    @Valid
    private List<OrderItemRequest> items;

    public CreateOrderRequest() {}

    public CreateOrderRequest(UUID sellerProfileId, List<OrderItemRequest> items) {
        this.sellerProfileId = sellerProfileId;
        this.items = items;
    }

    public UUID getSellerProfileId() {
        return sellerProfileId;
    }

    public void setSellerProfileId(UUID sellerProfileId) {
        this.sellerProfileId = sellerProfileId;
    }

    public List<OrderItemRequest> getItems() {
        return items;
    }

    public void setItems(List<OrderItemRequest> items) {
        this.items = items;
    }

    public static class OrderItemRequest {
        @NotNull(message = "Product ID is required")
        private UUID productId;

        @NotNull(message = "Quantity is required")
        private Integer quantity;

        public OrderItemRequest() {}

        public OrderItemRequest(UUID productId, Integer quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }

        public UUID getProductId() {
            return productId;
        }

        public void setProductId(UUID productId) {
            this.productId = productId;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
    }
}
