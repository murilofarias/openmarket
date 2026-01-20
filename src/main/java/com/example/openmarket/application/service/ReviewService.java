package com.example.openmarket.application.service;

import com.example.openmarket.application.domain.*;
import com.example.openmarket.application.exception.DomainException;
import com.example.openmarket.application.exception.ResourceNotFoundException;
import com.example.openmarket.application.port.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final BuyerProfileRepository buyerRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public ReviewService(ReviewRepository reviewRepository,
                        ProductRepository productRepository,
                        BuyerProfileRepository buyerRepository,
                        OrderRepository orderRepository,
                        UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
        this.buyerRepository = buyerRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    /**
     * Create review (buyer only, must have purchased product)
     */
    public UUID createReview(String userId, UUID productId, UUID orderId,
                            Integer rating, String comment) {
        // Get buyer profile
        BuyerProfile buyer = buyerRepository.findByUserId(userId)
            .orElseThrow(() -> new DomainException("User does not have a buyer profile"));

        // Verify product exists
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product", productId.toString()));

        // Verify order exists and belongs to buyer
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order", orderId.toString()));

        if (!order.getBuyerProfileId().equals(buyer.getId())) {
            throw new DomainException("You can only review products you purchased");
        }

        // Verify order contains this product
        boolean containsProduct = order.getItems().stream()
            .anyMatch(item -> item.getProductId().equals(productId));

        if (!containsProduct) {
            throw new DomainException("This order does not contain the product");
        }

        // Check if already reviewed
        if (reviewRepository.existsByBuyerAndProductAndOrder(buyer.getId(), productId, orderId)) {
            throw new DomainException("You have already reviewed this product for this order");
        }

        // Get buyer name from User cache
        User user = userRepository.findByExternalAuthId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        // Create review with buyer name
        Review review = Review.create(productId, buyer.getId(), user.getName(), orderId, rating, comment);
        Review saved = reviewRepository.save(review);

        // Recalculate product rating (synchronous)
        recalculateProductRating(productId);

        return saved.getId();
    }

    /**
     * Get reviews for a product (all)
     */
    @Transactional(readOnly = true)
    public List<Review> getProductReviews(UUID productId) {
        return reviewRepository.findByProductId(productId);
    }

    /**
     * Get reviews for a product (paginated)
     */
    @Transactional(readOnly = true)
    public Page<Review> getProductReviewsPaginated(UUID productId, Pageable pageable) {
        return reviewRepository.findByProductId(productId, pageable);
    }

    /**
     * Get single review by ID
     */
    @Transactional(readOnly = true)
    public Review findReviewById(UUID id) {
        return reviewRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Review", id.toString()));
    }

    /**
     * Recalculate product rating from all reviews
     */
    private void recalculateProductRating(UUID productId) {
        BigDecimal avgRating = reviewRepository.calculateAverageRating(productId);
        int count = reviewRepository.countByProductId(productId);

        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product", productId.toString()));

        product.recalculateRating(avgRating, count);
        productRepository.save(product);
    }
}
