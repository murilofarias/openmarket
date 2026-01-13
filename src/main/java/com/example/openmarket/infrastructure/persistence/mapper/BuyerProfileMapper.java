package com.example.openmarket.infrastructure.persistence.mapper;

import com.example.openmarket.application.domain.BuyerProfile;
import com.example.openmarket.infrastructure.persistence.entity.BuyerProfileEntity;
import org.springframework.stereotype.Component;

@Component
public class BuyerProfileMapper {

    public BuyerProfileEntity toEntity(BuyerProfile domain) {
        BuyerProfileEntity entity = new BuyerProfileEntity();
        entity.setId(domain.getId());
        entity.setUserId(domain.getUserId());
        entity.setDefaultShippingAddress(domain.getDefaultShippingAddress());
        entity.setTotalOrders(domain.getTotalOrders());
        entity.setRating(domain.getRating());
        // createdAt and updatedAt set by JPA lifecycle callbacks
        return entity;
    }

    public BuyerProfile toDomain(BuyerProfileEntity entity) {
        return BuyerProfile.reconstitute(
            entity.getId(),
            entity.getUserId(),
            entity.getDefaultShippingAddress(),
            entity.getTotalOrders(),
            entity.getRating(),
            entity.getCreatedAt()
        );
    }
}
