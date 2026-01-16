package com.example.openmarket.infrastructure.persistence.mapper;

import com.example.openmarket.application.domain.OrderItem;
import com.example.openmarket.infrastructure.persistence.entity.OrderItemEntity;
import org.springframework.stereotype.Component;

@Component
public class OrderItemMapper {

    public OrderItemEntity toEntity(OrderItem domain) {
        OrderItemEntity entity = new OrderItemEntity();
        entity.setProductId(domain.getProductId());
        entity.setProductName(domain.getProductName());
        entity.setProductDescription(domain.getProductDescription());
        entity.setProductImageUrl(domain.getProductImageUrl());
        entity.setQuantity(domain.getQuantity());
        entity.setPrice(domain.getPrice());
        return entity;
    }

    public OrderItem toDomain(OrderItemEntity entity) {
        return new OrderItem(
                entity.getProductId(),
                entity.getProductName(),
                entity.getProductDescription(),
                entity.getProductImageUrl(),
                entity.getQuantity(),
                entity.getPrice()
        );
    }
}
