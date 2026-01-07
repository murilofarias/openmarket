package com.example.openmarket.infrastructure.persistence.entity;

import com.example.openmarket.application.domain.OrderStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "orders")
public class OrderEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private OrderStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "buyer_account_id", nullable = false)
    private UUID buyerAccountId;

    @Column(name = "seller_account_id", nullable = false)
    private UUID sellerAccountId;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "order_id", nullable = false)
    private List<OrderItemEntity> orderItems = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_account_id", nullable = false, insertable = false, updatable = false)
    private AccountEntity buyer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_account_id", nullable = false, insertable = false, updatable = false)
    private AccountEntity seller;

    public UUID getId() {
        return id;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public UUID getBuyerAccountId() {
        return buyerAccountId;
    }

    public UUID getSellerAccountId() {
        return sellerAccountId;
    }

    public List<OrderItemEntity> getOrderItems() {
        return orderItems;
    }

    public AccountEntity getBuyer() {
        return buyer;
    }

    public AccountEntity getSeller() {
        return seller;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setBuyerAccountId(UUID buyerAccountId) {
        this.buyerAccountId = buyerAccountId;
    }

    public void setSellerAccountId(UUID sellerAccountId) {
        this.sellerAccountId = sellerAccountId;
    }

    public void setOrderItems(List<OrderItemEntity> orderItems) {
        this.orderItems = orderItems;
    }
}
