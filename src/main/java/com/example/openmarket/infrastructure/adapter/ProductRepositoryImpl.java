package com.example.openmarket.infrastructure.adapter;

import com.example.openmarket.application.domain.Product;
import com.example.openmarket.application.port.ProductRepository;
import com.example.openmarket.infrastructure.persistence.entity.ProductEntity;
import com.example.openmarket.infrastructure.persistence.mapper.ProductMapper;
import com.example.openmarket.infrastructure.persistence.repository.ProductJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class ProductRepositoryImpl implements ProductRepository {

    private final ProductJpaRepository jpaRepository;
    private final ProductMapper mapper;

    public ProductRepositoryImpl(ProductJpaRepository jpaRepository, ProductMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Product save(Product product) {
        ProductEntity entity = mapper.toEntity(product);
        ProductEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Product> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public void delete(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public Page<Product> searchProducts(String keyword, String category,
                                        BigDecimal minPrice, BigDecimal maxPrice,
                                        Pageable pageable) {
        Page<ProductEntity> entities = jpaRepository.searchProducts(
            keyword, category, minPrice, maxPrice, pageable
        );
        return entities.map(mapper::toDomain);
    }

    @Override
    public List<Product> findBySellerId(UUID sellerProfileId) {
        return jpaRepository.findBySellerProfileId(sellerProfileId).stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }
}
