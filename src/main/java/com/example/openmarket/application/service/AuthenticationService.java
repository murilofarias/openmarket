package com.example.openmarket.application.service;

import com.example.openmarket.application.domain.User;
import com.example.openmarket.application.port.AuthResult;
import com.example.openmarket.application.port.IdentityProvider;
import com.example.openmarket.application.port.UserRepository;
import com.example.openmarket.controller.dto.response.LoginResponse;
import org.springframework.stereotype.Service;

/**
 * Service for authentication-related operations.
 * Delegates to the IdentityProvider abstraction.
 */
@Service
public class AuthenticationService {

    private final IdentityProvider identityProvider;
    private final UserRepository userRepository;

    public AuthenticationService(IdentityProvider identityProvider, UserRepository userRepository) {
        this.identityProvider = identityProvider;
        this.userRepository = userRepository;
    }

    /**
     * Registers a new user in the identity provider and creates a local User cache.
     *
     * @param email User's email
     * @param password User's password
     * @param name User's full name
     * @return The userId (subject) from the identity provider
     */
    public String registerUser(String email, String password, String name) {
        String externalAuthId = identityProvider.createUser(email, password, name);

        User user = User.create(externalAuthId, email, name);
        userRepository.save(user);

        return externalAuthId;
    }

    /**
     * Authenticates a user and returns access token with user info.
     *
     * @param email User's email
     * @param password User's password
     * @return LoginResponse with tokens and user information
     */
    public LoginResponse login(String email, String password) {
        AuthResult result = identityProvider.authenticate(email, password);
        return toLoginResponse(result);
    }

    /**
     * Refreshes an access token using a refresh token.
     *
     * @param refreshToken The refresh token
     * @return LoginResponse with new tokens
     */
    public LoginResponse refreshToken(String refreshToken) {
        AuthResult result = identityProvider.refreshToken(refreshToken);
        return toLoginResponse(result);
    }

    /**
     * Logs out a user by invalidating the refresh token.
     *
     * @param refreshToken The refresh token to invalidate
     */
    public void logout(String refreshToken) {
        identityProvider.revokeToken(refreshToken);
    }

    private LoginResponse toLoginResponse(AuthResult result) {
        LoginResponse.UserInfo user = new LoginResponse.UserInfo(
            result.user().id(),
            result.user().email(),
            result.user().name()
        );

        return new LoginResponse(
            result.accessToken(),
            result.refreshToken(),
            result.expiresIn(),
            result.tokenType(),
            user
        );
    }
}
