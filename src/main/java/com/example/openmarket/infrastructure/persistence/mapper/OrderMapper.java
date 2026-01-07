package com.example.openmarket.infrastructure.persistence.mapper;

import com.example.openmarket.application.domain.Order;
import com.example.openmarket.application.domain.OrderItem;
import com.example.openmarket.infrastructure.persistence.entity.OrderEntity;
import com.example.openmarket.infrastructure.persistence.entity.OrderItemEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    private final OrderItemMapper orderItemMapper;

    public OrderMapper(OrderItemMapper orderItemMapper) {
        this.orderItemMapper = orderItemMapper;
    }

    public OrderEntity toEntity(Order domain) {
        OrderEntity entity = new OrderEntity();
        entity.setId(domain.getId());
        entity.setBuyerAccountId(domain.getBuyerAccountId());
        entity.setSellerAccountId(domain.getSellerAccountId());
        entity.setTotal(domain.getTotal());
        entity.setStatus(domain.getStatus());
        entity.setCreatedAt(domain.getCreatedAt());

        List<OrderItemEntity> orderItemEntities = domain.getItems().stream()
                .map(orderItemMapper::toEntity)
                .collect(Collectors.toList());
        entity.setOrderItems(orderItemEntities);

        return entity;
    }

    public Order toDomain(OrderEntity entity) {
        List<OrderItem> orderItems = entity.getOrderItems().stream()
                .map(orderItemMapper::toDomain)
                .collect(Collectors.toList());

        return Order.reconstitute(
                entity.getId(),
                entity.getBuyerAccountId(),
                entity.getSellerAccountId(),
                orderItems,
                entity.getTotal(),
                entity.getStatus(),
                entity.getCreatedAt()
        );
    }
}
