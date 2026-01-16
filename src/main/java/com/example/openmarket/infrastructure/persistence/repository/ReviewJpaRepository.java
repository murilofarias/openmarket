package com.example.openmarket.infrastructure.persistence.repository;

import com.example.openmarket.infrastructure.persistence.entity.ReviewEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface ReviewJpaRepository extends JpaRepository<ReviewEntity, UUID> {

    List<ReviewEntity> findByProductId(UUID productId);

    Page<ReviewEntity> findByProductId(UUID productId, Pageable pageable);

    boolean existsByBuyerProfileIdAndProductIdAndOrderId(UUID buyerProfileId, UUID productId, UUID orderId);

    @Query("SELECT AVG(r.rating) FROM ReviewEntity r WHERE r.productId = :productId")
    BigDecimal calculateAverageRating(@Param("productId") UUID productId);

    int countByProductId(UUID productId);
}
