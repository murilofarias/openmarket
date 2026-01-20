package com.example.openmarket.application.port;

import com.example.openmarket.application.domain.SellerProfile;
import com.example.openmarket.application.domain.SellerStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SellerProfileRepository {

    SellerProfile save(SellerProfile sellerProfile);

    Optional<SellerProfile> findById(UUID id);

    Optional<SellerProfile> findByUserId(String userId);

    boolean existsByUserId(String userId);

    List<SellerProfile> findByStatus(SellerStatus status);

    void deleteById(UUID id);
}
