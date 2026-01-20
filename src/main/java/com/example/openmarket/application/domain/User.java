package com.example.openmarket.application.domain;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * User entity - lightweight cache of user information from external auth provider.
 * This avoids frequent calls to Keycloak/external services for basic user info.
 */
public class User {

    private UUID id;
    private final String externalAuthId;  // Keycloak subject/user ID
    private String email;
    private String name;
    private final LocalDateTime createdAt;

    private User(UUID id, String externalAuthId, String email, String name, LocalDateTime createdAt) {
        this.id = id;
        this.externalAuthId = externalAuthId;
        this.email = email;
        this.name = name;
        this.createdAt = createdAt;
    }

    public static User create(String externalAuthId, String email, String name) {
        validateExternalAuthId(externalAuthId);
        validateEmail(email);
        validateName(name);
        return new User(UUID.randomUUID(), externalAuthId, email, name, null);
    }

    public static User reconstitute(UUID id, String externalAuthId, String email,
                                    String name, LocalDateTime createdAt) {
        return new User(id, externalAuthId, email, name, createdAt);
    }

    public void updateInfo(String email, String name) {
        validateEmail(email);
        validateName(name);
        this.email = email;
        this.name = name;
    }

    // Validation
    private static void validateExternalAuthId(String externalAuthId) {
        if (externalAuthId == null || externalAuthId.trim().isEmpty()) {
            throw new IllegalArgumentException("External auth ID cannot be empty");
        }
    }

    private static void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        // Basic email validation
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }

    private static void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        if (name.length() < 2 || name.length() > 100) {
            throw new IllegalArgumentException("Name must be between 2 and 100 characters");
        }
    }

    // Getters
    public UUID getId() { return id; }
    public String getExternalAuthId() { return externalAuthId; }
    public String getEmail() { return email; }
    public String getName() { return name; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
