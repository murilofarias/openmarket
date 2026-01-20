package com.example.openmarket.infrastructure.persistence.mapper;

import com.example.openmarket.application.domain.Review;
import com.example.openmarket.infrastructure.persistence.entity.ReviewEntity;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {

    public ReviewEntity toEntity(Review domain) {
        ReviewEntity entity = new ReviewEntity();
        entity.setId(domain.getId());
        entity.setProductId(domain.getProductId());
        entity.setBuyerProfileId(domain.getBuyerProfileId());
        entity.setBuyerName(domain.getBuyerName());
        entity.setOrderId(domain.getOrderId());
        entity.setRating(domain.getRating());
        entity.setComment(domain.getComment());
        // createdAt and updatedAt will be set automatically by JPA lifecycle callbacks

        return entity;
    }

    public Review toDomain(ReviewEntity entity) {
        return Review.reconstitute(
                entity.getId(),
                entity.getProductId(),
                entity.getBuyerProfileId(),
                entity.getBuyerName(),
                entity.getOrderId(),
                entity.getRating(),
                entity.getComment(),
                entity.getCreatedAt()
        );
    }
}
