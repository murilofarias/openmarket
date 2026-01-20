package com.example.openmarket.application.port;

import com.example.openmarket.application.domain.BuyerProfile;

import java.util.Optional;
import java.util.UUID;

public interface BuyerProfileRepository {

    BuyerProfile save(BuyerProfile buyerProfile);

    Optional<BuyerProfile> findById(UUID id);

    Optional<BuyerProfile> findByUserId(String userId);

    boolean existsByUserId(String userId);

    void deleteById(UUID id);
}
