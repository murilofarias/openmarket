package com.example.openmarket.controller;

import com.example.openmarket.application.domain.AuthenticatedUser;
import com.example.openmarket.application.domain.Review;
import com.example.openmarket.application.service.ReviewService;
import com.example.openmarket.controller.dto.request.CreateReviewRequest;
import com.example.openmarket.controller.dto.response.CreateReviewResponse;
import com.example.openmarket.controller.dto.response.ReviewResponse;
import com.example.openmarket.infrastructure.security.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/products/{productId}/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /**
     * Create review for a product (PROTECTED - buyer only, must have purchased)
     * POST /products/{productId}/reviews
     */
    @PostMapping
    public ResponseEntity<CreateReviewResponse> createReview(
            @PathVariable UUID productId,
            @Valid @RequestBody CreateReviewRequest request,
            @CurrentUser AuthenticatedUser user) {

        UUID reviewId = reviewService.createReview(
            user.getUserId(),
            productId,
            request.getOrderId(),
            request.getRating(),
            request.getComment()
        );

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(new CreateReviewResponse(reviewId));
    }

    /**
     * Get reviews for a product (PUBLIC)
     * GET /products/{productId}/reviews?page=0&size=20&sort=rating,desc
     */
    @GetMapping
    public ResponseEntity<Page<ReviewResponse>> getProductReviews(
            @PathVariable UUID productId,
            @PageableDefault(size = 10, sort = "rating") Pageable pageable) {

        Page<Review> reviews = reviewService.getProductReviewsPaginated(productId, pageable);
        Page<ReviewResponse> response = reviews.map(this::toReviewResponse);

        return ResponseEntity.ok(response);
    }

    // Helper method to map domain to DTO
    private ReviewResponse toReviewResponse(Review review) {
        return new ReviewResponse(
            review.getId(),
            review.getProductId(),
            review.getBuyerProfileId(),
            review.getBuyerName(),
            review.getRating(),
            review.getComment(),
            review.getCreatedAt()
        );
    }
}
