package com.example.openmarket.controller.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class OrderResponse {

    private UUID id;
    private UUID buyerProfileId;
    private UUID sellerProfileId;
    private List<OrderItemResponse> items;
    private BigDecimal total;
    private String status;
    private String metadata;
    private LocalDateTime createdAt;

    public OrderResponse() {}

    public OrderResponse(UUID id, UUID buyerProfileId, UUID sellerProfileId,
                        List<OrderItemResponse> items, BigDecimal total,
                        String status, String metadata, LocalDateTime createdAt) {
        this.id = id;
        this.buyerProfileId = buyerProfileId;
        this.sellerProfileId = sellerProfileId;
        this.items = items;
        this.total = total;
        this.status = status;
        this.metadata = metadata;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getBuyerProfileId() {
        return buyerProfileId;
    }

    public void setBuyerProfileId(UUID buyerProfileId) {
        this.buyerProfileId = buyerProfileId;
    }

    public UUID getSellerProfileId() {
        return sellerProfileId;
    }

    public void setSellerProfileId(UUID sellerProfileId) {
        this.sellerProfileId = sellerProfileId;
    }

    public List<OrderItemResponse> getItems() {
        return items;
    }

    public void setItems(List<OrderItemResponse> items) {
        this.items = items;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public static class OrderItemResponse {
        private UUID productId;
        private String productName;
        private String productDescription;
        private String productImageUrl;
        private Integer quantity;
        private BigDecimal price;
        private BigDecimal subtotal;

        public OrderItemResponse() {}

        public OrderItemResponse(UUID productId, String productName, String productDescription,
                                String productImageUrl, Integer quantity, BigDecimal price,
                                BigDecimal subtotal) {
            this.productId = productId;
            this.productName = productName;
            this.productDescription = productDescription;
            this.productImageUrl = productImageUrl;
            this.quantity = quantity;
            this.price = price;
            this.subtotal = subtotal;
        }

        public UUID getProductId() {
            return productId;
        }

        public void setProductId(UUID productId) {
            this.productId = productId;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public String getProductDescription() {
            return productDescription;
        }

        public void setProductDescription(String productDescription) {
            this.productDescription = productDescription;
        }

        public String getProductImageUrl() {
            return productImageUrl;
        }

        public void setProductImageUrl(String productImageUrl) {
            this.productImageUrl = productImageUrl;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public BigDecimal getSubtotal() {
            return subtotal;
        }

        public void setSubtotal(BigDecimal subtotal) {
            this.subtotal = subtotal;
        }
    }
}
