package com.example.openmarket.controller;

import com.example.openmarket.application.domain.AuthenticatedUser;
import com.example.openmarket.application.domain.Product;
import com.example.openmarket.application.domain.SellerProfile;
import com.example.openmarket.application.service.ProductService;
import com.example.openmarket.controller.dto.request.CreateProductRequest;
import com.example.openmarket.controller.dto.request.UpdateProductRequest;
import com.example.openmarket.controller.dto.response.CreateProductResponse;
import com.example.openmarket.controller.dto.response.ProductResponse;
import com.example.openmarket.infrastructure.security.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Search/filter products (PUBLIC)
     * GET /products?keyword=laptop&category=Electronics&minPrice=100&maxPrice=1000&page=0&size=20
     */
    @GetMapping
    public ResponseEntity<Page<ProductResponse>> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        Page<Product> products = productService.searchProducts(keyword, category,
                                                               minPrice, maxPrice, pageable);

        // Map each product to ProductResponse without seller info (performance optimization)
        Page<ProductResponse> response = products.map(this::toProductResponse);


        return ResponseEntity.ok(response);
    }

    /**
     * Get product by ID with embedded seller info (PUBLIC)
     * GET /products/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable UUID id) {
        Product product = productService.findProductById(id);
        SellerProfile seller = productService.getProductSeller(id);

        ProductResponse response = toProductResponseWithSeller(product, seller);

        return ResponseEntity.ok(response);
    }

    /**
     * Create new product (PROTECTED - seller only)
     * POST /products
     */
    @PostMapping
    public ResponseEntity<CreateProductResponse> createProduct(
            @Valid @RequestBody CreateProductRequest request,
            @CurrentUser AuthenticatedUser user) {

        UUID productId = productService.createProduct(
            user.getUserId(),
            request.getName(),
            request.getDescription(),
            request.getPrice(),
            request.getStock(),
            request.getCategory(),
            request.getImageUrls()
        );

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(new CreateProductResponse(productId));
    }

    /**
     * Publish product (PROTECTED - seller only, must own product)
     * POST /products/{id}/publish
     */
    @PostMapping("/{id}/publish")
    public ResponseEntity<Void> publishProduct(
            @PathVariable UUID id,
            @CurrentUser AuthenticatedUser user) {

        productService.publishProduct(id, user.getUserId());

        URI location = ServletUriComponentsBuilder
            .fromCurrentContextPath()
            .path("/products/{id}")
            .buildAndExpand(id)
            .toUri();

        return ResponseEntity.ok()
            .location(location)
            .build();
    }

    /**
     * Update product partially (PROTECTED - seller only, must own product)
     * PATCH /products/{id}
     */
    @PatchMapping("/{id}")
    public ResponseEntity<Void> updateProduct(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProductRequest request,
            @CurrentUser AuthenticatedUser user) {

        productService.updateProduct(
            id,
            user.getUserId(),
            request.getName(),
            request.getDescription(),
            request.getPrice(),
            request.getStock(),
            request.getCategory()
        );

        return ResponseEntity.noContent().build();
    }

    // Helper methods to map domain to DTO

    private ProductResponse toProductResponse(Product product) {
        List<ProductResponse.ProductImageDto> imageDtos = product.getImages().stream()
            .map(img -> new ProductResponse.ProductImageDto(
                img.getUrl(),
                img.getPosition(),
                img.isPrimary()
            ))
            .collect(Collectors.toList());

        return new ProductResponse(
            product.getId(),
            product.getName(),
            product.getDescription(),
            product.getPrice(),
            product.getStock(),
            product.getCategory(),
            product.getStatus().name(),
            product.getRating(),
            product.getReviewCount(),
            imageDtos,
            null,  // No seller info in search results
            product.getCreatedAt()
        );
    }

    private ProductResponse toProductResponseWithSeller(Product product, SellerProfile seller) {
        ProductResponse.SellerInfoDto sellerInfo = new ProductResponse.SellerInfoDto(
            seller.getId(),
            seller.getStoreName(),
            seller.getStoreDescription(),
            seller.getRating()
        );

        List<ProductResponse.ProductImageDto> imageDtos = product.getImages().stream()
            .map(img -> new ProductResponse.ProductImageDto(
                img.getUrl(),
                img.getPosition(),
                img.isPrimary()
            ))
            .collect(Collectors.toList());

        return new ProductResponse(
            product.getId(),
            product.getName(),
            product.getDescription(),
            product.getPrice(),
            product.getStock(),
            product.getCategory(),
            product.getStatus().name(),
            product.getRating(),
            product.getReviewCount(),
            imageDtos,
            sellerInfo,
            product.getCreatedAt()
        );
    }
}
