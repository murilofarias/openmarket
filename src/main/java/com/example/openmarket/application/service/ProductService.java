package com.example.openmarket.application.service;

import com.example.openmarket.application.domain.Product;
import com.example.openmarket.application.domain.SellerProfile;
import com.example.openmarket.application.exception.DomainException;
import com.example.openmarket.application.exception.ResourceNotFoundException;
import com.example.openmarket.application.port.ProductRepository;
import com.example.openmarket.application.port.SellerProfileRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ProductService {

    private static final int MAX_IMAGES_PER_PRODUCT = 5;

    private final ProductRepository productRepository;
    private final SellerProfileRepository sellerRepository;
    private final ImageService imageService;

    public ProductService(ProductRepository productRepository,
                         SellerProfileRepository sellerRepository,
                         ImageService imageService) {
        this.productRepository = productRepository;
        this.sellerRepository = sellerRepository;
        this.imageService = imageService;
    }

    /**
     * Create new product (seller only, must be ACTIVE)
     */
    public UUID createProduct(String userId, String name, String description,
                             BigDecimal price, Integer stock, String category) {
        SellerProfile seller = sellerRepository.findByUserId(userId)
            .orElseThrow(() -> new DomainException("User does not have a seller profile"));

        if (!seller.isActive()) {
            throw new DomainException("Only active sellers can create products. Your seller account status is: " + seller.getStatus());
        }

        Product product = Product.create(seller.getId(), name, description,
                                        price, stock, category);

        Product saved = productRepository.save(product);
        return saved.getId();
    }

    /**
     * Publish product (seller only, must own product)
     */
    public void publishProduct(UUID productId, String userId) {
        // Get seller profile
        SellerProfile seller = sellerRepository.findByUserId(userId)
            .orElseThrow(() -> new DomainException("User does not have a seller profile"));

        // Verify seller is active
        if (!seller.isActive()) {
            throw new DomainException("Only active sellers can publish products");
        }

        // Get product
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product", productId.toString()));

        // Verify ownership
        if (!product.getSellerProfileId().equals(seller.getId())) {
            throw new DomainException("You can only publish your own products");
        }

        // Publish product
        product.publish();

        productRepository.save(product);
    }

    /**
     * Update product partially (seller only, must own product)
     */
    public void updateProduct(UUID productId, String userId, String name,
                             String description, BigDecimal price, Integer stock,
                             String category) {
        // Get seller profile
        SellerProfile seller = sellerRepository.findByUserId(userId)
            .orElseThrow(() -> new DomainException("User does not have a seller profile"));

        // Verify seller is active
        if (!seller.isActive()) {
            throw new DomainException("Only active sellers can update products");
        }

        // Get product
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product", productId.toString()));

        // Verify ownership
        if (!product.getSellerProfileId().equals(seller.getId())) {
            throw new DomainException("You can only update your own products");
        }

        // Update fields (partial update)
        product.updateFromRequest(name, description, price, stock, category);

        productRepository.save(product);
    }

    /**
     * Search products (public)
     */
    @Transactional(readOnly = true)
    public Page<Product> searchProducts(String keyword, String category,
                                       BigDecimal minPrice, BigDecimal maxPrice,
                                       Pageable pageable) {
        return productRepository.searchProducts(keyword, category, minPrice, maxPrice, pageable);
    }

    /**
     * Find product by ID (public)
     */
    @Transactional(readOnly = true)
    public Product findProductById(UUID id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product", id.toString()));
    }

    /**
     * Get seller for a product (used for embedding seller info in response)
     */
    @Transactional(readOnly = true)
    public SellerProfile getProductSeller(UUID productId) {
        Product product = findProductById(productId);
        return sellerRepository.findById(product.getSellerProfileId())
            .orElseThrow(() -> new ResourceNotFoundException("SellerProfile",
                        product.getSellerProfileId().toString()));
    }

    /**
     * Find products by seller ID
     */
    @Transactional(readOnly = true)
    public List<Product> findProductsBySeller(UUID sellerProfileId) {
        return productRepository.findBySellerId(sellerProfileId);
    }

    /**
     * Add image to product (seller only, must own product).
     * Validates ownership and image limit BEFORE uploading the file.
     *
     * @return the URL of the uploaded image
     */
    public String addImageToProduct(UUID productId, String userId, MultipartFile file) {
        SellerProfile seller = sellerRepository.findByUserId(userId)
            .orElseThrow(() -> new DomainException("User does not have a seller profile"));

        if (!seller.isActive()) {
            throw new DomainException("Only active sellers can add images to products");
        }

        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product", productId.toString()));

        if (!product.getSellerProfileId().equals(seller.getId())) {
            throw new DomainException("You can only add images to your own products");
        }

        if (product.getImages().size() >= MAX_IMAGES_PER_PRODUCT) {
            throw new DomainException("Maximum " + MAX_IMAGES_PER_PRODUCT + " images allowed per product");
        }

        // All validations passed, now upload the image
        String imageUrl = imageService.uploadImage(file);

        product.addImage(imageUrl);
        productRepository.save(product);

        return imageUrl;
    }
}
