package com.example.openmarket.application.port;

import com.example.openmarket.application.domain.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(UUID id);

    Optional<User> findByExternalAuthId(String externalAuthId);

    Optional<User> findByEmail(String email);

    boolean existsByExternalAuthId(String externalAuthId);

    boolean existsByEmail(String email);

    void deleteById(UUID id);
}
