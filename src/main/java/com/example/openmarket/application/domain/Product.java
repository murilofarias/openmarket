package com.example.openmarket.application.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Product {

    private UUID id;
    private UUID sellerAccountId;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private ProductStatus status;
    private List<ProductImage> images = new ArrayList<>();
    private LocalDateTime createdAt;

    private Product(UUID id, UUID sellerAccountId, String name, String description,
                    BigDecimal price, Integer stock) {
        this.id = id;
        this.sellerAccountId = sellerAccountId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.status = ProductStatus.DRAFT;
        this.createdAt = LocalDateTime.now();
    }

    private Product(UUID id, UUID sellerAccountId, String name, String description, BigDecimal price, Integer stock,
                    ProductStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.sellerAccountId = sellerAccountId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static Product create(UUID sellerProfileId, String name, String description,
                                 BigDecimal price, Integer stock) {
        validatePrice(price);
        validateStock(stock);
        return new Product(UUID.randomUUID(), sellerProfileId, name, description, price, stock);
    }

    public static Product reconstitute(UUID id, UUID sellerProfileId, String name, String description, BigDecimal price,
                                       Integer stock,
                                       ProductStatus status, LocalDateTime createdAt) {
        return new Product(id, sellerProfileId, name, description, price, stock, status, createdAt);
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

    public void addImage(String url) {
        if (images.size() >= 5) {
            throw new IllegalStateException("Maximum 5 images allowed");
        }

        boolean primary = images.isEmpty();
        images.add(new ProductImage(url, images.size(), primary));
    }

    public UUID getId() { return id; }
    public UUID getSellerAccountId() { return sellerAccountId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public Integer getStock() { return stock; }
    public ProductStatus getStatus() { return status; }
    public List<ProductImage> getImages() { return images; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
