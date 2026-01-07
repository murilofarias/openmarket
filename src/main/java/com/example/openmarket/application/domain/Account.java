package com.example.openmarket.application.domain;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Account {

    private final UUID id;
    private Set<Profile> profiles;
    private String name;
    private String email;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String passwordHash;

    private Account(UUID id, String email, String passwordHash, String name) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.profiles = new HashSet<>();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    private Account(UUID id, String email, String passwordHash, String name,
                    Set<Profile> profiles, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.name = name;
        this.profiles = profiles;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Account create(String email, String passwordHash, String name) {
        validateEmail(email);
        validateName(name);
        return new Account(UUID.randomUUID(), email, passwordHash, name);
    }

    // Reconstitution (from database)
    public static Account reconstitute(UUID id, String email, String passwordHash,
                                       String name, Set<Profile> profiles, LocalDateTime createdAt,
                                       LocalDateTime updatedAt) {
        return new Account(id, email, passwordHash, name, profiles, createdAt, updatedAt);
    }

    // Business methods
    public void addProfile(Profile profile) {
        if (hasRole(profile.getProfileRole())) {
            throw new IllegalStateException("Profile with role " + profile.getProfileRole() + " already exists");
        }
        profiles.add(profile);
    }

    public boolean hasRole(Role role) {
        return profiles.stream()
                .anyMatch(p -> p.getProfileRole() == role);
    }

    public Profile getProfileByRole(Role role) {
        return profiles.stream()
                .filter(p -> p.getProfileRole() == role)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No profile with role: " + role));
    }

    public void updateName(String newName) {
        validateName(newName);
        this.name = newName;
        this.updatedAt = LocalDateTime.now();
    }

    private static void validateEmail(String email) {
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }

    private static void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
    }

    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public String getName() { return name; }
    public String getPasswordHash() { return passwordHash; }
    public Set<Profile> getProfiles() { return new HashSet<>(profiles); } // Defensive copy
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
