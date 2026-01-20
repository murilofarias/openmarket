package com.example.openmarket.application.port;

import com.example.openmarket.application.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository {

    Product save(Product product);

    Optional<Product> findById(UUID id);

    void delete(UUID id);

    Page<Product> searchProducts(String keyword, String category,
                                 BigDecimal minPrice, BigDecimal maxPrice,
                                 Pageable pageable);

    List<Product> findBySellerId(UUID sellerProfileId);
}
