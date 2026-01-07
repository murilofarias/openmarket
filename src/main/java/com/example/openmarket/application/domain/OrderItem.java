package com.example.openmarket.application.domain;

import java.math.BigDecimal;
import java.util.UUID;

public class OrderItem {

    private UUID productId;
    private int quantity;
    private BigDecimal price;

    public OrderItem(UUID productId, int quantity, BigDecimal priceAtPurchase) {
        this.productId = productId;
        this.quantity = quantity;
        this.price = priceAtPurchase;
    }

    public BigDecimal getSubtotal() {
        return price.multiply(new BigDecimal(quantity));
    }

    public UUID getProductId() { return productId; }
    public Integer getQuantity() { return quantity; }
}
