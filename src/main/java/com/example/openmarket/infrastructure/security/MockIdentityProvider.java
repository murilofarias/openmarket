package com.example.openmarket.infrastructure.security;

import com.example.openmarket.application.port.IdentityProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Mock implementation of IdentityProvider for development/testing.
 * Replace with KeycloakIdentityProvider in production.
 */
@Component
@Profile({"dev", "test"})
public class MockIdentityProvider implements IdentityProvider {

    private static final Logger log = LoggerFactory.getLogger(MockIdentityProvider.class);

    private final Map<String, UserData> users = new HashMap<>();

    @Override
    public String createUser(String email, String password, String name) {
        String userId = UUID.randomUUID().toString();
        users.put(userId, new UserData(email, name, new HashSet<>(), true));
        log.info("Mock: Created user {} with email {}", userId, email);
        return userId;
    }

    @Override
    public void assignRoles(String userId, Set<String> roles) {
        UserData user = getUserOrThrow(userId);
        user.roles.addAll(roles);
        log.info("Mock: Assigned roles {} to user {}", roles, userId);
    }

    @Override
    public void addRole(String userId, String role) {
        UserData user = getUserOrThrow(userId);
        user.roles.add(role);
        log.info("Mock: Added role {} to user {}", role, userId);
    }

    @Override
    public void removeRole(String userId, String role) {
        UserData user = getUserOrThrow(userId);
        user.roles.remove(role);
        log.info("Mock: Removed role {} from user {}", role, userId);
    }

    @Override
    public boolean userExists(String userId) {
        boolean exists = users.containsKey(userId);
        log.debug("Mock: User {} exists: {}", userId, exists);
        return exists;
    }

    @Override
    public void enableUser(String userId) {
        UserData user = getUserOrThrow(userId);
        user.enabled = true;
        log.info("Mock: Enabled user {}", userId);
    }

    @Override
    public void disableUser(String userId) {
        UserData user = getUserOrThrow(userId);
        user.enabled = false;
        log.info("Mock: Disabled user {}", userId);
    }

    private UserData getUserOrThrow(String userId) {
        UserData user = users.get(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + userId);
        }
        return user;
    }

    private static class UserData {
        String email;
        String name;
        Set<String> roles;
        boolean enabled;

        UserData(String email, String name, Set<String> roles, boolean enabled) {
            this.email = email;
            this.name = name;
            this.roles = roles;
            this.enabled = enabled;
        }
    }
}
