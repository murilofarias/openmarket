package com.example.openmarket.application.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Product {

    private UUID id;
    private UUID sellerProfileId;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private String category;
    private ProductStatus status;
    private BigDecimal rating;
    private Integer reviewCount;
    private List<ProductImage> images = new ArrayList<>();
    private LocalDateTime createdAt;

    private Product(UUID id, UUID sellerProfileId, String name, String description,
                    BigDecimal price, Integer stock, String category) {
        this.id = id;
        this.sellerProfileId = sellerProfileId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.category = category;
        this.status = ProductStatus.DRAFT;
        this.rating = null;
        this.reviewCount = 0;
        this.createdAt = null; // Will be set by database
    }

    private Product(UUID id, UUID sellerProfileId, String name, String description, BigDecimal price, Integer stock,
                    String category, ProductStatus status, BigDecimal rating, Integer reviewCount, LocalDateTime createdAt) {
        this.id = id;
        this.sellerProfileId = sellerProfileId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.category = category;
        this.status = status;
        this.rating = rating;
        this.reviewCount = reviewCount;
        this.createdAt = createdAt;
    }

    public static Product create(UUID sellerProfileId, String name, String description,
                                 BigDecimal price, Integer stock, String category) {
        validatePrice(price);
        validateStock(stock);
        validateCategory(category);
        return new Product(UUID.randomUUID(), sellerProfileId, name, description, price, stock, category);
    }

    public static Product reconstitute(UUID id, UUID sellerProfileId, String name, String description, BigDecimal price,
                                       Integer stock, String category, ProductStatus status, BigDecimal rating,
                                       Integer reviewCount, LocalDateTime createdAt) {
        return new Product(id, sellerProfileId, name, description, price, stock, category, status, rating, reviewCount, createdAt);
    }

    // Business logic
    public void reduceStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (stock < quantity) {
            throw new IllegalStateException("Insufficient stock. Available: " + stock);
        }
        stock -= quantity;

        if (stock == 0) {
            this.status = ProductStatus.OUT_OF_STOCK;
        }
    }

    public void addStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        stock += quantity;

        // If product was out of stock, reactivate it
        if (this.status == ProductStatus.OUT_OF_STOCK && stock > 0) {
            this.status = ProductStatus.ACTIVE;
        }
    }

    public void publish() {
        if (stock <= 0) {
            throw new IllegalStateException("Cannot publish product with no stock");
        }

        if (price.compareTo(new BigDecimal(0)) <= 0) {
            throw new IllegalStateException("Cannot publish product with invalid price");
        }

        this.status = ProductStatus.ACTIVE;
    }

    public boolean isAvailable() {
        return status == ProductStatus.ACTIVE && stock > 0;
    }

    public void updateCategory(String category) {
        validateCategory(category);
        this.category = category;
    }

    public void updateFromRequest(String name, String description, BigDecimal price,
                                   Integer stock, String category) {
        if (name != null) {
            this.name = name;
        }
        if (description != null) {
            this.description = description;
        }
        if (price != null) {
            validatePrice(price);
            this.price = price;
        }
        if (stock != null) {
            validateStock(stock);
            this.stock = stock;
            // Update status based on stock
            if (this.stock == 0) {
                this.status = ProductStatus.OUT_OF_STOCK;
            } else if (this.status == ProductStatus.OUT_OF_STOCK) {
                this.status = ProductStatus.ACTIVE;
            }
        }
        if (category != null) {
            validateCategory(category);
            this.category = category;
        }
    }

    public void recalculateRating(BigDecimal averageRating, int count) {
        if (count < 0) {
            throw new IllegalArgumentException("Review count cannot be negative");
        }
        if (count == 0) {
            this.rating = null;
            this.reviewCount = 0;
        } else {
            if (averageRating == null || averageRating.compareTo(BigDecimal.ZERO) < 0 || averageRating.compareTo(new BigDecimal("5")) > 0) {
                throw new IllegalArgumentException("Rating must be between 0 and 5");
            }
            this.rating = averageRating;
            this.reviewCount = count;
        }
    }

    // Validation
    private static void validatePrice(BigDecimal price) {
        if (price == null || price.compareTo(new BigDecimal(0)) <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
    }

    private static void validateStock(Integer stock) {
        if (stock == null || stock < 0) {
            throw new IllegalArgumentException("Stock cannot be negative");
        }
    }

    private static void validateCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("Category cannot be empty");
        }
        if (category.length() < 3 || category.length() > 50) {
            throw new IllegalArgumentException("Category must be between 3 and 50 characters");
        }
    }

    public void addImage(String url) {
        if (images.size() >= 5) {
            throw new IllegalStateException("Maximum 5 images allowed");
        }

        boolean primary = images.isEmpty();
        images.add(new ProductImage(url, images.size(), primary));
    }

    public UUID getId() { return id; }
    public UUID getSellerProfileId() { return sellerProfileId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public Integer getStock() { return stock; }
    public String getCategory() { return category; }
    public ProductStatus getStatus() { return status; }
    public BigDecimal getRating() { return rating; }
    public Integer getReviewCount() { return reviewCount; }
    public List<ProductImage> getImages() { return images; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
