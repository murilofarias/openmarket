package com.example.openmarket.infrastructure.persistence.mapper;

import com.example.openmarket.application.domain.Product;
import com.example.openmarket.application.domain.ProductImage;
import com.example.openmarket.infrastructure.persistence.entity.ProductEntity;
import com.example.openmarket.infrastructure.persistence.entity.ProductImageEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProductMapper {

    private final ProductImageMapper productImageMapper;

    public ProductMapper(ProductImageMapper productImageMapper) {
        this.productImageMapper = productImageMapper;
    }

    public ProductEntity toEntity(Product domain) {
        ProductEntity entity = new ProductEntity();
        entity.setId(domain.getId());
        entity.setSellerProfileId(domain.getSellerProfileId());
        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
        entity.setPrice(domain.getPrice());
        entity.setStock(domain.getStock());
        entity.setCategory(domain.getCategory());
        entity.setStatus(domain.getStatus());
        entity.setRating(domain.getRating());
        entity.setReviewCount(domain.getReviewCount());
        // createdAt and updatedAt will be set automatically by JPA lifecycle callbacks

        List<ProductImageEntity> imageEntities = domain.getImages().stream()
                .map(productImageMapper::toEntity)
                .collect(Collectors.toList());
        entity.setProductImages(imageEntities);

        return entity;
    }

    public Product toDomain(ProductEntity entity) {
        List<ProductImage> images = entity.getProductImages().stream()
                .map(productImageMapper::toDomain)
                .collect(Collectors.toList());

        return Product.reconstitute(
                entity.getId(),
                entity.getSellerProfileId(),
                entity.getName(),
                entity.getDescription(),
                entity.getPrice(),
                entity.getStock(),
                entity.getCategory(),
                entity.getStatus(),
                entity.getRating(),
                entity.getReviewCount(),
                images,
                entity.getCreatedAt()
        );
    }
}
