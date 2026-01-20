package com.example.openmarket.infrastructure.adapter;

import com.example.openmarket.application.domain.Order;
import com.example.openmarket.application.port.OrderRepository;
import com.example.openmarket.infrastructure.persistence.entity.OrderEntity;
import com.example.openmarket.infrastructure.persistence.mapper.OrderMapper;
import com.example.openmarket.infrastructure.persistence.repository.OrderJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderJpaRepository jpaRepository;
    private final OrderMapper mapper;

    public OrderRepositoryImpl(OrderJpaRepository jpaRepository, OrderMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Order save(Order order) {
        OrderEntity entity = mapper.toEntity(order);
        OrderEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Order> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Page<Order> findByBuyerProfileId(UUID buyerProfileId, Pageable pageable) {
        Page<OrderEntity> entities = jpaRepository.findByBuyerProfileId(buyerProfileId, pageable);
        return entities.map(mapper::toDomain);
    }

    @Override
    public Page<Order> findBySellerProfileId(UUID sellerProfileId, Pageable pageable) {
        Page<OrderEntity> entities = jpaRepository.findBySellerProfileId(sellerProfileId, pageable);
        return entities.map(mapper::toDomain);
    }

    @Override
    public void delete(UUID id) {
        jpaRepository.deleteById(id);
    }
}
