package com.example.openmarket.application.port;

import java.util.Set;

/**
 * Port for Identity Provider integration (e.g., Keycloak, Auth0, etc.)
 * This abstraction allows the domain to be independent of the specific IAM solution.
 */
public interface IdentityProvider {

    /**
     * Creates a new user in the identity provider.
     *
     * @param email User's email
     * @param password User's password
     * @param name User's full name
     * @return The userId (subject) from the identity provider
     */
    String createUser(String email, String password, String name);

    /**
     * Authenticates a user and returns tokens.
     *
     * @param email User's email
     * @param password User's password
     * @return AuthResult with tokens and user info
     */
    AuthResult authenticate(String email, String password);

    /**
     * Refreshes an access token using a refresh token.
     *
     * @param refreshToken The refresh token
     * @return AuthResult with new tokens
     */
    AuthResult refreshToken(String refreshToken);

    /**
     * Revokes/invalidates a refresh token (logout).
     *
     * @param refreshToken The refresh token to revoke
     */
    void revokeToken(String refreshToken);

    /**
     * Assigns roles to a user.
     *
     * @param userId The user's ID in the identity provider
     * @param roles Set of role names to assign (e.g., "BUYER", "SELLER")
     */
    void assignRoles(String userId, Set<String> roles);

    /**
     * Adds a single role to a user.
     *
     * @param userId The user's ID in the identity provider
     * @param role Role name to add (e.g., "SELLER")
     */
    void addRole(String userId, String role);

    /**
     * Removes a role from a user.
     *
     * @param userId The user's ID in the identity provider
     * @param role Role name to remove
     */
    void removeRole(String userId, String role);

    /**
     * Checks if a user exists in the identity provider.
     *
     * @param userId The user's ID
     * @return true if user exists, false otherwise
     */
    boolean userExists(String userId);

    /**
     * Enables a user account.
     *
     * @param userId The user's ID
     */
    void enableUser(String userId);

    /**
     * Disables a user account.
     *
     * @param userId The user's ID
     */
    void disableUser(String userId);
}
