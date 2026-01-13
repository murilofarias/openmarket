package com.example.openmarket.infrastructure.persistence.repository;

import com.example.openmarket.application.domain.SellerProfile;
import com.example.openmarket.application.domain.SellerStatus;
import com.example.openmarket.application.port.SellerProfileRepository;
import com.example.openmarket.infrastructure.persistence.entity.SellerProfileEntity;
import com.example.openmarket.infrastructure.persistence.mapper.SellerProfileMapper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

interface SpringDataSellerProfileRepository extends JpaRepository<SellerProfileEntity, UUID> {
    Optional<SellerProfileEntity> findByUserId(String userId);
    boolean existsByUserId(String userId);
    List<SellerProfileEntity> findByStatus(SellerStatus status);
}

@Repository
public class JpaSellerProfileRepository implements SellerProfileRepository {

    private final SpringDataSellerProfileRepository jpaRepository;
    private final SellerProfileMapper mapper;

    public JpaSellerProfileRepository(
            SpringDataSellerProfileRepository jpaRepository,
            SellerProfileMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public SellerProfile save(SellerProfile profile) {
        SellerProfileEntity entity = mapper.toEntity(profile);
        SellerProfileEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<SellerProfile> findById(UUID id) {
        return jpaRepository.findById(id)
            .map(mapper::toDomain);
    }

    @Override
    public Optional<SellerProfile> findByUserId(String userId) {
        return jpaRepository.findByUserId(userId)
            .map(mapper::toDomain);
    }

    @Override
    public boolean existsByUserId(String userId) {
        return jpaRepository.existsByUserId(userId);
    }

    @Override
    public List<SellerProfile> findByStatus(SellerStatus status) {
        return jpaRepository.findByStatus(status).stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}
