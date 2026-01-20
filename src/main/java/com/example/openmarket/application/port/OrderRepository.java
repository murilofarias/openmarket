package com.example.openmarket.application.port;

import com.example.openmarket.application.domain.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findById(UUID id);

    Page<Order> findByBuyerProfileId(UUID buyerProfileId, Pageable pageable);

    Page<Order> findBySellerProfileId(UUID sellerProfileId, Pageable pageable);

    void delete(UUID id);
}
