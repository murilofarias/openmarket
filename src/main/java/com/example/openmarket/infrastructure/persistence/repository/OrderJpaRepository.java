package com.example.openmarket.infrastructure.persistence.repository;

import com.example.openmarket.infrastructure.persistence.entity.OrderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderJpaRepository extends JpaRepository<OrderEntity, UUID> {

    Page<OrderEntity> findByBuyerProfileId(UUID buyerProfileId, Pageable pageable);

    Page<OrderEntity> findBySellerProfileId(UUID sellerProfileId, Pageable pageable);
}
