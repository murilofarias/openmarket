package com.example.openmarket.application.port;

import com.example.openmarket.application.domain.Product;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository {

    Product save(Product product);

    Optional<Product> findById(UUID id);

    void delete(UUID id);
}
