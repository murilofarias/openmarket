package com.example.openmarket.infrastructure.persistence.repository;

import com.example.openmarket.application.domain.User;
import com.example.openmarket.application.port.UserRepository;
import com.example.openmarket.infrastructure.persistence.entity.UserEntity;
import com.example.openmarket.infrastructure.persistence.mapper.UserMapper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

interface SpringDataUserRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByExternalAuthId(String externalAuthId);
    Optional<UserEntity> findByEmail(String email);
    boolean existsByExternalAuthId(String externalAuthId);
    boolean existsByEmail(String email);
}

@Repository
public class JpaUserRepository implements UserRepository {

    private final SpringDataUserRepository jpaRepository;
    private final UserMapper mapper;

    public JpaUserRepository(SpringDataUserRepository jpaRepository,
                            UserMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public User save(User user) {
        UserEntity entity = mapper.toEntity(user);
        UserEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpaRepository.findById(id)
            .map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByExternalAuthId(String externalAuthId) {
        return jpaRepository.findByExternalAuthId(externalAuthId)
            .map(mapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email)
            .map(mapper::toDomain);
    }

    @Override
    public boolean existsByExternalAuthId(String externalAuthId) {
        return jpaRepository.existsByExternalAuthId(externalAuthId);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}
