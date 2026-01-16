package com.example.openmarket.infrastructure.persistence.repository;

import com.example.openmarket.application.domain.Review;
import com.example.openmarket.application.port.ReviewRepository;
import com.example.openmarket.infrastructure.persistence.entity.ReviewEntity;
import com.example.openmarket.infrastructure.persistence.mapper.ReviewMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class JpaReviewRepository implements ReviewRepository {

    private final ReviewJpaRepository jpaRepository;
    private final ReviewMapper mapper;

    public JpaReviewRepository(ReviewJpaRepository jpaRepository,
                              ReviewMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Review save(Review review) {
        ReviewEntity entity = mapper.toEntity(review);
        ReviewEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Review> findById(UUID id) {
        return jpaRepository.findById(id)
            .map(mapper::toDomain);
    }

    @Override
    public List<Review> findByProductId(UUID productId) {
        return jpaRepository.findByProductId(productId).stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public Page<Review> findByProductId(UUID productId, Pageable pageable) {
        Page<ReviewEntity> entities = jpaRepository.findByProductId(productId, pageable);
        return entities.map(mapper::toDomain);
    }

    @Override
    public boolean existsByBuyerAndProductAndOrder(UUID buyerProfileId, UUID productId, UUID orderId) {
        return jpaRepository.existsByBuyerProfileIdAndProductIdAndOrderId(buyerProfileId, productId, orderId);
    }

    @Override
    public BigDecimal calculateAverageRating(UUID productId) {
        BigDecimal average = jpaRepository.calculateAverageRating(productId);
        return average != null ? average : BigDecimal.ZERO;
    }

    @Override
    public int countByProductId(UUID productId) {
        return jpaRepository.countByProductId(productId);
    }
}
