package com.example.openmarket.application.port;

/**
 * Authentication result from the identity provider.
 * This is an abstraction that works with any IAM solution.
 */
public record AuthResult(
    String accessToken,
    String refreshToken,
    int expiresIn,
    String tokenType,
    UserInfo user
) {
    public record UserInfo(
        String id,
        String email,
        String name
    ) {}
}
