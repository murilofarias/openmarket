package com.example.openmarket.controller.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ProductResponse {

    private UUID id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private String category;
    private String status;
    private BigDecimal rating;
    private Integer reviewCount;
    private List<ProductImageDto> images;
    private SellerInfoDto seller;
    private LocalDateTime createdAt;

    public ProductResponse() {}

    public ProductResponse(UUID id, String name, String description, BigDecimal price,
                          Integer stock, String category, String status, BigDecimal rating,
                          Integer reviewCount, List<ProductImageDto> images,
                          SellerInfoDto seller, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.category = category;
        this.status = status;
        this.rating = rating;
        this.reviewCount = reviewCount;
        this.images = images;
        this.seller = seller;
        this.createdAt = createdAt;
    }

    // Nested DTO for seller information
    public static class SellerInfoDto {
        private UUID id;
        private String storeName;
        private String storeDescription;
        private BigDecimal rating;

        public SellerInfoDto() {}

        public SellerInfoDto(UUID id, String storeName, String storeDescription, BigDecimal rating) {
            this.id = id;
            this.storeName = storeName;
            this.storeDescription = storeDescription;
            this.rating = rating;
        }

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }

        public String getStoreName() { return storeName; }
        public void setStoreName(String storeName) { this.storeName = storeName; }

        public String getStoreDescription() { return storeDescription; }
        public void setStoreDescription(String storeDescription) { this.storeDescription = storeDescription; }

        public BigDecimal getRating() { return rating; }
        public void setRating(BigDecimal rating) { this.rating = rating; }
    }

    // Nested DTO for product images
    public static class ProductImageDto {
        private String url;
        private int position;
        private boolean primary;

        public ProductImageDto() {}

        public ProductImageDto(String url, int position, boolean primary) {
            this.url = url;
            this.position = position;
            this.primary = primary;
        }

        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }

        public int getPosition() { return position; }
        public void setPosition(int position) { this.position = position; }

        public boolean isPrimary() { return primary; }
        public void setPrimary(boolean primary) { this.primary = primary; }
    }

    // Getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public BigDecimal getRating() { return rating; }
    public void setRating(BigDecimal rating) { this.rating = rating; }

    public Integer getReviewCount() { return reviewCount; }
    public void setReviewCount(Integer reviewCount) { this.reviewCount = reviewCount; }

    public List<ProductImageDto> getImages() { return images; }
    public void setImages(List<ProductImageDto> images) { this.images = images; }

    public SellerInfoDto getSeller() { return seller; }
    public void setSeller(SellerInfoDto seller) { this.seller = seller; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
