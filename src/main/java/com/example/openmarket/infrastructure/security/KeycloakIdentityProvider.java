package com.example.openmarket.infrastructure.security;

import com.example.openmarket.application.port.AuthResult;
import com.example.openmarket.application.port.IdentityProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
@Primary
public class KeycloakIdentityProvider implements IdentityProvider {

    private static final Logger log = LoggerFactory.getLogger(KeycloakIdentityProvider.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${keycloak.server-url}")
    private String serverUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.client-id:openmarket-api}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    private String tokenUrl;
    private String logoutUrl;
    private String adminUsersUrl;

    // Cached service token for admin operations
    private String cachedServiceToken;
    private long tokenExpiresAt;

    public KeycloakIdentityProvider(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostConstruct
    public void init() {
        this.tokenUrl = serverUrl + "/realms/" + realm + "/protocol/openid-connect/token";
        this.logoutUrl = serverUrl + "/realms/" + realm + "/protocol/openid-connect/logout";
        this.adminUsersUrl = serverUrl + "/admin/realms/" + realm + "/users";

        // Warm-up: obtain service token on startup to pre-establish connection
        warmUp();
    }

    private void warmUp() {
        try {
            log.info("Warming up Keycloak connection...");
            long start = System.currentTimeMillis();
            getServiceToken();
            long elapsed = System.currentTimeMillis() - start;
            log.info("Keycloak warm-up completed in {}ms, token cached for {}s", elapsed, (tokenExpiresAt - System.currentTimeMillis()) / 1000);
        } catch (Exception e) {
            log.warn("Keycloak warm-up failed (will retry on first request): {}", e.getMessage());
        }
    }

    @Override
    public String createUser(String email, String password, String name) {
        String[] nameParts = name.split(" ", 2);

        Map<String, Object> credential = Map.of(
            "type", "password",
            "value", password,
            "temporary", false
        );

        Map<String, Object> userPayload = new HashMap<>();
        userPayload.put("username", email);
        userPayload.put("email", email);
        userPayload.put("emailVerified", true);
        userPayload.put("enabled", true);
        userPayload.put("firstName", nameParts[0]);
        if (nameParts.length > 1) {
            userPayload.put("lastName", nameParts[1]);
        }
        userPayload.put("credentials", Collections.singletonList(credential));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(getServiceToken());

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(userPayload, headers);

        ResponseEntity<Void> response = restTemplate.postForEntity(adminUsersUrl, request, Void.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to create user: " + response.getStatusCode());
        }

        // Extract user ID from Location header
        String location = response.getHeaders().getLocation().toString();
        return location.substring(location.lastIndexOf('/') + 1);
    }

    @Override
    public AuthResult authenticate(String email, String password) {
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

        return toAuthResult(response.getBody());
    }

    @Override
    public AuthResult refreshToken(String refreshToken) {
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

        return toAuthResult(response.getBody());
    }

    @Override
    public void revokeToken(String refreshToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("refresh_token", refreshToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        restTemplate.postForEntity(logoutUrl, request, Void.class);
    }

    @Override
    public void assignRoles(String userId, Set<String> roles) {
        for (String role : roles) {
            addRole(userId, role);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void addRole(String userId, String role) {
        // Get role representation
        String roleUrl = serverUrl + "/admin/realms/" + realm + "/roles/" + role;
        HttpHeaders headers = createAdminHeaders();

        ResponseEntity<Map> roleResponse = restTemplate.exchange(
            roleUrl, HttpMethod.GET,
            new HttpEntity<>(headers), Map.class);

        Map<String, Object> roleRep = roleResponse.getBody();

        // Add role to user
        String userRolesUrl = adminUsersUrl + "/" + userId + "/role-mappings/realm";
        HttpEntity<Object> request = new HttpEntity<>(Collections.singletonList(roleRep), headers);
        restTemplate.postForEntity(userRolesUrl, request, Void.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void removeRole(String userId, String role) {
        // Get role representation
        String roleUrl = serverUrl + "/admin/realms/" + realm + "/roles/" + role;
        HttpHeaders headers = createAdminHeaders();

        ResponseEntity<Map> roleResponse = restTemplate.exchange(
            roleUrl, HttpMethod.GET,
            new HttpEntity<>(headers), Map.class);

        Map<String, Object> roleRep = roleResponse.getBody();

        // Remove role from user
        String userRolesUrl = adminUsersUrl + "/" + userId + "/role-mappings/realm";
        HttpEntity<Object> request = new HttpEntity<>(Collections.singletonList(roleRep), headers);
        restTemplate.exchange(userRolesUrl, HttpMethod.DELETE, request, Void.class);
    }

    @Override
    public boolean userExists(String userId) {
        try {
            String userUrl = adminUsersUrl + "/" + userId;
            HttpHeaders headers = createAdminHeaders();
            restTemplate.exchange(userUrl, HttpMethod.GET,
                new HttpEntity<>(headers), Map.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void enableUser(String userId) {
        updateUserEnabled(userId, true);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void disableUser(String userId) {
        updateUserEnabled(userId, false);
    }

    private void updateUserEnabled(String userId, boolean enabled) {
        String userUrl = adminUsersUrl + "/" + userId;
        HttpHeaders headers = createAdminHeaders();

        // Get current user
        ResponseEntity<Map> response = restTemplate.exchange(
            userUrl, HttpMethod.GET,
            new HttpEntity<>(headers), Map.class);

        Map<String, Object> user = new HashMap<>(response.getBody());
        user.put("enabled", enabled);

        // Update user
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(user, headers);
        restTemplate.exchange(userUrl, HttpMethod.PUT, request, Void.class);
    }

    private HttpHeaders createAdminHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(getServiceToken());
        return headers;
    }

    @SuppressWarnings("unchecked")
    private AuthResult toAuthResult(Map<String, Object> tokenResponse) {
        String accessToken = (String) tokenResponse.get("access_token");
        Map<String, Object> claims = decodeJwtPayload(accessToken);

        AuthResult.UserInfo userInfo = new AuthResult.UserInfo(
            (String) claims.get("sub"),
            (String) claims.get("email"),
            (String) claims.get("name")
        );

        return new AuthResult(
            accessToken,
            (String) tokenResponse.get("refresh_token"),
            (Integer) tokenResponse.get("expires_in"),
            (String) tokenResponse.get("token_type"),
            userInfo
        );
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> decodeJwtPayload(String jwt) {
        try {
            String[] parts = jwt.split("\\.");
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            return objectMapper.readValue(payload, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decode JWT", e);
        }
    }

    @SuppressWarnings("unchecked")
    private synchronized String getServiceToken() {
        // Return cached token if still valid (with 30s buffer)
        if (cachedServiceToken != null && System.currentTimeMillis() < tokenExpiresAt - 30000) {
            return cachedServiceToken;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("Failed to obtain service token");
        }

        Map<String, Object> tokenResponse = response.getBody();
        cachedServiceToken = (String) tokenResponse.get("access_token");
        int expiresIn = (Integer) tokenResponse.get("expires_in");
        tokenExpiresAt = System.currentTimeMillis() + (expiresIn * 1000L);

        return cachedServiceToken;
    }
}
