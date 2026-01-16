package com.example.openmarket.application.service;

import com.example.openmarket.application.domain.*;
import com.example.openmarket.application.exception.DomainException;
import com.example.openmarket.application.exception.ResourceNotFoundException;
import com.example.openmarket.application.port.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final BuyerProfileRepository buyerRepository;
    private final SellerProfileRepository sellerRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository,
                       BuyerProfileRepository buyerRepository,
                       SellerProfileRepository sellerRepository,
                       ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.buyerRepository = buyerRepository;
        this.sellerRepository = sellerRepository;
        this.productRepository = productRepository;
    }

    /**
     * Create order (buyer only).
     * Snapshots product data at time of order creation.
     */
    public UUID createOrder(String userId, UUID sellerProfileId, List<OrderItemRequest> itemRequests) {
        // Validate buyer exists
        BuyerProfile buyer = buyerRepository.findByUserId(userId)
            .orElseThrow(() -> new DomainException("User does not have a buyer profile"));

        // Validate seller exists
        SellerProfile seller = sellerRepository.findById(sellerProfileId)
            .orElseThrow(() -> new ResourceNotFoundException("Seller", sellerProfileId.toString()));

        // Create order items with product snapshots
        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderItemRequest itemRequest : itemRequests) {
            Product product = productRepository.findById(itemRequest.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", itemRequest.getProductId().toString()));

            // Validate product belongs to the seller
            if (!product.getSellerProfileId().equals(sellerProfileId)) {
                throw new DomainException("Product does not belong to the specified seller");
            }

            // Validate stock availability
            if (product.getStock() < itemRequest.getQuantity()) {
                throw new DomainException("Insufficient stock for product: " + product.getName());
            }

            // Get primary image URL (or null if no images)
            String imageUrl = product.getImages().stream()
                .filter(ProductImage::isPrimary)
                .findFirst()
                .map(ProductImage::getUrl)
                .orElse(product.getImages().isEmpty() ? null : product.getImages().get(0).getUrl());

            // Create order item with product snapshot
            OrderItem orderItem = new OrderItem(
                product.getId(),
                product.getName(),
                product.getDescription(),
                imageUrl,
                itemRequest.getQuantity(),
                product.getPrice()
            );
            orderItems.add(orderItem);

            // Reduce product stock
            product.reduceStock(itemRequest.getQuantity());
            productRepository.save(product);
        }

        // Create order
        Order order = Order.create(buyer.getId(), sellerProfileId, orderItems);
        Order saved = orderRepository.save(order);

        return saved.getId();
    }

    /**
     * Get orders for a user (buyer or seller).
     * Filters based on user's role.
     */
    @Transactional(readOnly = true)
    public Page<Order> getMyOrders(String userId, String role, Pageable pageable) {
        if ("BUYER".equals(role)) {
            BuyerProfile buyer = buyerRepository.findByUserId(userId)
                .orElseThrow(() -> new DomainException("User does not have a buyer profile"));
            return orderRepository.findByBuyerProfileId(buyer.getId(), pageable);
        } else if ("SELLER".equals(role)) {
            SellerProfile seller = sellerRepository.findByUserId(userId)
                .orElseThrow(() -> new DomainException("User does not have a seller profile"));
            return orderRepository.findBySellerProfileId(seller.getId(), pageable);
        } else {
            throw new IllegalArgumentException("Invalid role: " + role);
        }
    }

    /**
     * Get order by ID (with access control).
     * User must be either the buyer or seller of the order.
     */
    @Transactional(readOnly = true)
    public Order getOrderById(String userId, UUID orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order", orderId.toString()));

        // Check if user has access to this order (as buyer or seller)
        BuyerProfile buyer = buyerRepository.findByUserId(userId).orElse(null);
        SellerProfile seller = sellerRepository.findByUserId(userId).orElse(null);

        boolean hasAccess = (buyer != null && order.getBuyerProfileId().equals(buyer.getId())) ||
                           (seller != null && order.getSellerProfileId().equals(seller.getId()));

        if (!hasAccess) {
            throw new DomainException("You do not have access to this order");
        }

        return order;
    }

    /**
     * Update order status (seller only).
     */
    public void updateOrderStatus(String userId, UUID orderId, OrderStatus newStatus) {
        // Validate seller exists
        SellerProfile seller = sellerRepository.findByUserId(userId)
            .orElseThrow(() -> new DomainException("User does not have a seller profile"));

        // Get order
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order", orderId.toString()));

        // Validate seller owns this order
        if (!order.getSellerProfileId().equals(seller.getId())) {
            throw new DomainException("You can only update status of your own orders");
        }

        // Update status
        order.updateStatus(newStatus);
        orderRepository.save(order);
    }

    /**
     * Cancel order (buyer or seller can cancel).
     */
    public void cancelOrder(String userId, UUID orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order", orderId.toString()));

        // Check if user has access to this order (as buyer or seller)
        BuyerProfile buyer = buyerRepository.findByUserId(userId).orElse(null);
        SellerProfile seller = sellerRepository.findByUserId(userId).orElse(null);

        boolean hasAccess = (buyer != null && order.getBuyerProfileId().equals(buyer.getId())) ||
                           (seller != null && order.getSellerProfileId().equals(seller.getId()));

        if (!hasAccess) {
            throw new DomainException("You do not have access to this order");
        }

        // Cancel order
        order.cancel();
        orderRepository.save(order);

        // Restore product stock
        for (OrderItem item : order.getItems()) {
            Product product = productRepository.findById(item.getProductId()).orElse(null);
            if (product != null) {
                product.addStock(item.getQuantity());
                productRepository.save(product);
            }
        }
    }

    /**
     * Helper class for order item requests.
     */
    public static class OrderItemRequest {
        private UUID productId;
        private Integer quantity;

        public OrderItemRequest() {}

        public OrderItemRequest(UUID productId, Integer quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }

        public UUID getProductId() { return productId; }
        public void setProductId(UUID productId) { this.productId = productId; }

        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }
}
