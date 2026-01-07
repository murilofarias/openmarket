package com.example.openmarket.infrastructure.persistence.mapper;

import com.example.openmarket.application.domain.ProductImage;
import com.example.openmarket.infrastructure.persistence.entity.ProductImageEntity;
import org.springframework.stereotype.Component;

@Component
public class ProductImageMapper {

    public ProductImageEntity toEntity(ProductImage domain) {
        ProductImageEntity entity = new ProductImageEntity();
        entity.setUrl(domain.getUrl());
        entity.setPosition(domain.getPosition());
        entity.setPrimary(domain.isPrimary());
        return entity;
    }

    public ProductImage toDomain(ProductImageEntity entity) {
        return new ProductImage(
                entity.getUrl(),
                entity.getPosition(),
                entity.isPrimary()
        );
    }
}
