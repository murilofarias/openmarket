package com.example.openmarket.application.domain;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * OrderItem - Snapshots product data at time of purchase.
 * Product updates don't affect historical order data.
 */
public class OrderItem {

    private UUID productId;
    private String productName;         // Product name at time of purchase
    private String productDescription;  // Product description at time of purchase (optional)
    private String productImageUrl;     // Primary product image URL at time of purchase (optional)
    private int quantity;
    private BigDecimal price;           // Price per unit at time of purchase

    public OrderItem(UUID productId, String productName, String productDescription,
                     String productImageUrl, int quantity, BigDecimal priceAtPurchase) {
        validateProductId(productId);
        validateProductName(productName);
        validateQuantity(quantity);
        validatePrice(priceAtPurchase);

        this.productId = productId;
        this.productName = productName;
        this.productDescription = productDescription;
        this.productImageUrl = productImageUrl;
        this.quantity = quantity;
        this.price = priceAtPurchase;
    }

    public BigDecimal getSubtotal() {
        return price.multiply(new BigDecimal(quantity));
    }

    // Validation
    private void validateProductId(UUID productId) {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
    }

    private void validateProductName(String productName) {
        if (productName == null || productName.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be empty");
        }
    }

    private void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
    }

    private void validatePrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be greater than 0");
        }
    }

    // Getters
    public UUID getProductId() { return productId; }
    public String getProductName() { return productName; }
    public String getProductDescription() { return productDescription; }
    public String getProductImageUrl() { return productImageUrl; }
    public Integer getQuantity() { return quantity; }
    public BigDecimal getPrice() { return price; }
}
