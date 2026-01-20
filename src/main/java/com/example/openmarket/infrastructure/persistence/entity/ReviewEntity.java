package com.example.openmarket.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "reviews",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_buyer_product_order",
        columnNames = {"buyer_profile_id", "product_id", "order_id"}
    ),
    indexes = {
        @Index(name = "idx_reviews_product", columnList = "product_id"),
        @Index(name = "idx_reviews_buyer", columnList = "buyer_profile_id")
    }
)
public class ReviewEntity extends BaseEntity {

    @Id
    private UUID id;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "buyer_profile_id", nullable = false)
    private UUID buyerProfileId;

    @Column(name = "buyer_name", nullable = false, length = 100)
    private String buyerName;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    // Navigation properties (optional, for lazy loading)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private ProductEntity product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_profile_id", insertable = false, updatable = false)
    private BuyerProfileEntity buyerProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", insertable = false, updatable = false)
    private OrderEntity order;

    public ReviewEntity() {}

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public UUID getBuyerProfileId() {
        return buyerProfileId;
    }

    public void setBuyerProfileId(UUID buyerProfileId) {
        this.buyerProfileId = buyerProfileId;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public ProductEntity getProduct() {
        return product;
    }

    public BuyerProfileEntity getBuyerProfile() {
        return buyerProfile;
    }

    public OrderEntity getOrder() {
        return order;
    }
}
