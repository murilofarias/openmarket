package com.example.openmarket.application.port;

import com.example.openmarket.application.domain.Order;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findById(UUID id);

    void delete(UUID id);
}
