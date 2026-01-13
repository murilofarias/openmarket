package com.example.openmarket.application.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class Order {

    private UUID id;
    private final UUID buyerProfileId;
    private final UUID sellerProfileId;
    private List<OrderItem> items;
    private BigDecimal total;
    private OrderStatus status;
    private final LocalDateTime createdAt;

    public Order(UUID buyerProfileId, UUID sellerProfileId, List<OrderItem> items) {
        this.buyerProfileId = buyerProfileId;
        this.sellerProfileId = sellerProfileId;
        this.createdAt = null; // Will be set by database
        this.items = items;
        calculateTotal();
    }

    public Order(UUID id, UUID buyerProfileId, UUID sellerProfileId, List<OrderItem> items, BigDecimal total, OrderStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.buyerProfileId = buyerProfileId;
        this.sellerProfileId = sellerProfileId;
        this.items = items;
        this.total = total;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static Order create(UUID buyerProfileId, UUID sellerProfileId, List<OrderItem> items) {
        return new Order(buyerProfileId, sellerProfileId, items);
    }

    public static Order reconstitute(UUID id, UUID buyerProfileId, UUID sellerProfileId, List<OrderItem> items,
                                       BigDecimal total, OrderStatus status, LocalDateTime createdAt) {
        return new Order(id, buyerProfileId, sellerProfileId, items, total, status, createdAt);
    }

    /*public void addItem(UUID productId, int quantity) {
        OrderItem item = new OrderItem(productId, quantity, product.getPrice());
        items.add(item);
        calculateTotal();
    }

    public void submit() {
        if (items.isEmpty()) {
            throw new IllegalStateException("Cannot submit empty order");
        }
        this.status = OrderStatus.CONFIRMED;

        //items.forEach(item -> item.getProduct().reduceStock(item.getQuantity()));
    }*/

    private void calculateTotal() {
        this.total = items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public UUID getId() { return id; }
    public UUID getBuyerProfileId() { return buyerProfileId; }
    public UUID getSellerProfileId() { return sellerProfileId; }
    public List<OrderItem> getItems() { return items; }
    public BigDecimal getTotal() { return total; }
    public OrderStatus getStatus() { return status; }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
