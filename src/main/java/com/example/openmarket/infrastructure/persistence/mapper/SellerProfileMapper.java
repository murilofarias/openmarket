package com.example.openmarket.infrastructure.persistence.mapper;

import com.example.openmarket.application.domain.SellerProfile;
import com.example.openmarket.infrastructure.persistence.entity.SellerProfileEntity;
import org.springframework.stereotype.Component;

@Component
public class SellerProfileMapper {

    public SellerProfileEntity toEntity(SellerProfile domain) {
        SellerProfileEntity entity = new SellerProfileEntity();
        entity.setId(domain.getId());
        entity.setUserId(domain.getUserId());
        entity.setStoreName(domain.getStoreName());
        entity.setStoreDescription(domain.getStoreDescription());
        entity.setStatus(domain.getStatus());
        entity.setRating(domain.getRating());
        // createdAt and updatedAt set by JPA lifecycle callbacks
        return entity;
    }

    public SellerProfile toDomain(SellerProfileEntity entity) {
        return SellerProfile.reconstitute(
            entity.getId(),
            entity.getUserId(),
            entity.getStoreName(),
            entity.getStoreDescription(),
            entity.getStatus(),
            entity.getRating(),
            entity.getCreatedAt()
        );
    }
}
