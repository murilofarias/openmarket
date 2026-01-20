package com.example.openmarket.infrastructure.persistence.repository;

import com.example.openmarket.application.domain.BuyerProfile;
import com.example.openmarket.application.port.BuyerProfileRepository;
import com.example.openmarket.infrastructure.persistence.entity.BuyerProfileEntity;
import com.example.openmarket.infrastructure.persistence.mapper.BuyerProfileMapper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

interface SpringDataBuyerProfileRepository extends JpaRepository<BuyerProfileEntity, UUID> {
    Optional<BuyerProfileEntity> findByUserId(String userId);
    boolean existsByUserId(String userId);
}

@Repository
public class JpaBuyerProfileRepository implements BuyerProfileRepository {

    private final SpringDataBuyerProfileRepository jpaRepository;
    private final BuyerProfileMapper mapper;

    public JpaBuyerProfileRepository(
            SpringDataBuyerProfileRepository jpaRepository,
            BuyerProfileMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public BuyerProfile save(BuyerProfile profile) {
        BuyerProfileEntity entity = mapper.toEntity(profile);
        BuyerProfileEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<BuyerProfile> findById(UUID id) {
        return jpaRepository.findById(id)
            .map(mapper::toDomain);
    }

    @Override
    public Optional<BuyerProfile> findByUserId(String userId) {
        return jpaRepository.findByUserId(userId)
            .map(mapper::toDomain);
    }

    @Override
    public boolean existsByUserId(String userId) {
        return jpaRepository.existsByUserId(userId);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}
