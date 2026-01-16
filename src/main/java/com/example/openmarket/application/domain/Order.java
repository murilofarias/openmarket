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
    private String metadata;  // JSON or text field for additional mutable data
    private final LocalDateTime createdAt;

    private Order(UUID buyerProfileId, UUID sellerProfileId, List<OrderItem> items) {
        this.id = UUID.randomUUID();
        this.buyerProfileId = buyerProfileId;
        this.sellerProfileId = sellerProfileId;
        this.createdAt = null; // Will be set by database
        this.items = items;
        this.status = OrderStatus.PENDING;
        calculateTotal();
    }

    private Order(UUID id, UUID buyerProfileId, UUID sellerProfileId, List<OrderItem> items,
                  BigDecimal total, OrderStatus status, String metadata, LocalDateTime createdAt) {
        this.id = id;
        this.buyerProfileId = buyerProfileId;
        this.sellerProfileId = sellerProfileId;
        this.items = items;
        this.total = total;
        this.status = status;
        this.metadata = metadata;
        this.createdAt = createdAt;
    }

    public static Order create(UUID buyerProfileId, UUID sellerProfileId, List<OrderItem> items) {
        return new Order(buyerProfileId, sellerProfileId, items);
    }

    public static Order reconstitute(UUID id, UUID buyerProfileId, UUID sellerProfileId, List<OrderItem> items,
                                       BigDecimal total, OrderStatus status, String metadata, LocalDateTime createdAt) {
        return new Order(id, buyerProfileId, sellerProfileId, items, total, status, metadata, createdAt);
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

    // Status transition methods (only status and metadata can be changed after creation)
    public void updateStatus(OrderStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }

        // Validate status transitions
        if (this.status == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Cannot change status of cancelled order");
        }
        if (this.status == OrderStatus.DELIVERED && newStatus != OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot change status of delivered order");
        }

        this.status = newStatus;
    }

    public void cancel() {
        if (this.status == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot cancel delivered order");
        }
        if (this.status == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Order is already cancelled");
        }
        this.status = OrderStatus.CANCELLED;
    }

    public void updateMetadata(String metadata) {
        this.metadata = metadata;
    }

    // Getters
    public UUID getId() { return id; }
    public UUID getBuyerProfileId() { return buyerProfileId; }
    public UUID getSellerProfileId() { return sellerProfileId; }
    public List<OrderItem> getItems() { return items; }
    public BigDecimal getTotal() { return total; }
    public OrderStatus getStatus() { return status; }
    public String getMetadata() { return metadata; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
