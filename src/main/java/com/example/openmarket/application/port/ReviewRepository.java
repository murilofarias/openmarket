package com.example.openmarket.application.port;

import com.example.openmarket.application.domain.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository {

    Review save(Review review);

    Optional<Review> findById(UUID id);

    List<Review> findByProductId(UUID productId);

    Page<Review> findByProductId(UUID productId, Pageable pageable);

    boolean existsByBuyerAndProductAndOrder(UUID buyerProfileId, UUID productId, UUID orderId);

    BigDecimal calculateAverageRating(UUID productId);

    int countByProductId(UUID productId);
}
