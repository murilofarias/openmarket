package com.example.openmarket.application.domain;

/**
 * Represents an authenticated user in the system.
 * This is a domain value object (not persisted) that carries
 * information about the currently authenticated user.
 */
public class AuthenticatedUser {

    private final String userId;
    private final String email;
    private final String name;

    public AuthenticatedUser(String userId, String email, String name) {
        this.userId = userId;
        this.email = email;
        this.name = name;
    }

    public String getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "AuthenticatedUser{" +
                "userId='" + userId + '\'' +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
