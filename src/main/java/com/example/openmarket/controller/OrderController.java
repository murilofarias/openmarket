package com.example.openmarket.controller;

import com.example.openmarket.application.domain.AuthenticatedUser;
import com.example.openmarket.application.domain.Order;
import com.example.openmarket.application.domain.OrderItem;
import com.example.openmarket.application.service.OrderService;
import com.example.openmarket.controller.dto.request.CreateOrderRequest;
import com.example.openmarket.controller.dto.request.UpdateOrderStatusRequest;
import com.example.openmarket.controller.dto.response.CreateOrderResponse;
import com.example.openmarket.controller.dto.response.OrderResponse;
import com.example.openmarket.infrastructure.security.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Create new order (PROTECTED - buyer only)
     * POST /orders
     */
    @PostMapping
    public ResponseEntity<CreateOrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @CurrentUser AuthenticatedUser user) {

        // Convert DTOs to service layer objects
        List<OrderService.OrderItemRequest> items = request.getItems().stream()
            .map(item -> new OrderService.OrderItemRequest(item.getProductId(), item.getQuantity()))
            .collect(Collectors.toList());

        UUID orderId = orderService.createOrder(
            user.getUserId(),
            request.getSellerProfileId(),
            items
        );

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(new CreateOrderResponse(orderId));
    }

    /**
     * Get user's orders (PROTECTED - buyer or seller)
     * GET /orders?role=BUYER&page=0&size=20
     * GET /orders?role=SELLER&page=0&size=20
     */
    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getMyOrders(
            @RequestParam String role,
            @CurrentUser AuthenticatedUser user,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        Page<Order> orders = orderService.getMyOrders(user.getUserId(), role, pageable);
        Page<OrderResponse> response = orders.map(this::toOrderResponse);

        return ResponseEntity.ok(response);
    }

    /**
     * Get order by ID (PROTECTED - only accessible by buyer or seller of the order)
     * GET /orders/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(
            @PathVariable UUID id,
            @CurrentUser AuthenticatedUser user) {

        Order order = orderService.getOrderById(user.getUserId(), id);
        OrderResponse response = toOrderResponse(order);

        return ResponseEntity.ok(response);
    }

    /**
     * Update order status (PROTECTED - seller only)
     * PATCH /orders/{id}/status
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateOrderStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateOrderStatusRequest request,
            @CurrentUser AuthenticatedUser user) {

        orderService.updateOrderStatus(user.getUserId(), id, request.getStatus());

        return ResponseEntity.noContent().build();
    }

    /**
     * Cancel order (PROTECTED - buyer or seller can cancel)
     * POST /orders/{id}/cancel
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelOrder(
            @PathVariable UUID id,
            @CurrentUser AuthenticatedUser user) {

        orderService.cancelOrder(user.getUserId(), id);

        return ResponseEntity.noContent().build();
    }

    // Helper method to map domain to DTO
    private OrderResponse toOrderResponse(Order order) {
        List<OrderResponse.OrderItemResponse> items = order.getItems().stream()
            .map(this::toOrderItemResponse)
            .collect(Collectors.toList());

        return new OrderResponse(
            order.getId(),
            order.getBuyerProfileId(),
            order.getSellerProfileId(),
            items,
            order.getTotal(),
            order.getStatus().name(),
            order.getMetadata(),
            order.getCreatedAt()
        );
    }

    private OrderResponse.OrderItemResponse toOrderItemResponse(OrderItem item) {
        return new OrderResponse.OrderItemResponse(
            item.getProductId(),
            item.getProductName(),
            item.getProductDescription(),
            item.getProductImageUrl(),
            item.getQuantity(),
            item.getPrice(),
            item.getSubtotal()
        );
    }
}
