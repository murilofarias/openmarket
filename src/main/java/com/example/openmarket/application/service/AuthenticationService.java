package com.example.openmarket.application.service;

import com.example.openmarket.application.domain.User;
import com.example.openmarket.application.port.IdentityProvider;
import com.example.openmarket.application.port.UserRepository;
import com.example.openmarket.controller.dto.response.LoginResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Map;

/**
 * Service for authentication-related operations.
 * Delegates to the IdentityProvider (Keycloak, Auth0, etc.)
 */
@Service
public class AuthenticationService {

    private final IdentityProvider identityProvider;
    private final RestTemplate restTemplate;
    private final UserRepository userRepository;

    @Value("${keycloak.server-url}")
    private String keycloakServerUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.client-id:openmarket-api}")
    private String clientId;

    @Value("${keycloak.client-secret:openmarket-api-secret}")
    private String clientSecret;

    private String tokenUrl;
    private String logoutUrl;

    public AuthenticationService(IdentityProvider identityProvider,
                                RestTemplate restTemplate,
                                UserRepository userRepository) {
        this.identityProvider = identityProvider;
        this.restTemplate = restTemplate;
        this.userRepository = userRepository;
    }

    @PostConstruct
    public void init() {
        this.tokenUrl = keycloakServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";
        this.logoutUrl = keycloakServerUrl + "/realms/" + realm + "/protocol/openid-connect/logout";
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
        // Create user in Keycloak
        String externalAuthId = identityProvider.createUser(email, password, name);

        // Create local User entity cache to avoid frequent Keycloak API calls
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

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("username", email);
        body.add("password", password);
        body.add("scope", "openid profile email");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Failed to authenticate user");
        }

        Map<String, Object> tokenResponse = response.getBody();

        // Decode JWT to extract user info
        String accessToken = (String) tokenResponse.get("access_token");
        Map<String, Object> userInfo = decodeJwtPayload(accessToken);

        LoginResponse.UserInfo user = new LoginResponse.UserInfo(
            (String) userInfo.get("sub"),
            (String) userInfo.get("email"),
            (String) userInfo.get("name")
        );

        return new LoginResponse(
            accessToken,
            (String) tokenResponse.get("refresh_token"),
            (Integer) tokenResponse.get("expires_in"),
            (String) tokenResponse.get("token_type"),
            user
        );
    }

    /**
     * Refreshes an access token using a refresh token.
     *
     * @param refreshToken The refresh token
     * @return LoginResponse with new tokens
     */
    public LoginResponse refreshToken(String refreshToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "refresh_token");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("refresh_token", refreshToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Failed to refresh token");
        }

        Map<String, Object> tokenResponse = response.getBody();

        // Decode JWT to extract user info
        String accessToken = (String) tokenResponse.get("access_token");
        Map<String, Object> userInfo = decodeJwtPayload(accessToken);

        LoginResponse.UserInfo user = new LoginResponse.UserInfo(
            (String) userInfo.get("sub"),
            (String) userInfo.get("email"),
            (String) userInfo.get("name")
        );

        return new LoginResponse(
            accessToken,
            (String) tokenResponse.get("refresh_token"),
            (Integer) tokenResponse.get("expires_in"),
            (String) tokenResponse.get("token_type"),
            user
        );
    }

    /**
     * Logs out a user by invalidating the refresh token.
     *
     * @param refreshToken The refresh token to invalidate
     */
    public void logout(String refreshToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("refresh_token", refreshToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        restTemplate.postForEntity(logoutUrl, request, Void.class);
    }

    /**
     * Decodes JWT payload to extract claims.
     */
    private Map<String, Object> decodeJwtPayload(String jwt) {
        try {
            String[] parts = jwt.split("\\.");
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));

            // Simple JSON parsing - in production, use Jackson ObjectMapper
            return parseSimpleJson(payload);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decode JWT", e);
        }
    }

    /**
     * Simple JSON parser for JWT payload.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseSimpleJson(String json) {
        // Use Jackson for proper parsing
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readValue(json, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON", e);
        }
    }
}
