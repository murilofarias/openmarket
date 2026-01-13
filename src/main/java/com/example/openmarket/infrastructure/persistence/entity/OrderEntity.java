package com.example.openmarket.infrastructure.persistence.entity;

import com.example.openmarket.application.domain.OrderStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "orders")
public class OrderEntity extends BaseEntity {

    @Id
    private UUID id;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private OrderStatus status;

    @Column(name = "buyer_profile_id", nullable = false)
    private UUID buyerProfileId;

    @Column(name = "seller_profile_id", nullable = false)
    private UUID sellerProfileId;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "order_id", nullable = false)
    private List<OrderItemEntity> orderItems = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_profile_id", nullable = false, insertable = false, updatable = false)
    private BuyerProfileEntity buyer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_profile_id", nullable = false, insertable = false, updatable = false)
    private SellerProfileEntity seller;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public BigDecimal getTotal() {
        return total;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public UUID getBuyerProfileId() {
        return buyerProfileId;
    }

    public UUID getSellerProfileId() {
        return sellerProfileId;
    }

    public List<OrderItemEntity> getOrderItems() {
        return orderItems;
    }

    public BuyerProfileEntity getBuyer() {
        return buyer;
    }

    public SellerProfileEntity getSeller() {
        return seller;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public void setBuyerProfileId(UUID buyerProfileId) {
        this.buyerProfileId = buyerProfileId;
    }

    public void setSellerProfileId(UUID sellerProfileId) {
        this.sellerProfileId = sellerProfileId;
    }

    public void setOrderItems(List<OrderItemEntity> orderItems) {
        this.orderItems = orderItems;
    }
}
