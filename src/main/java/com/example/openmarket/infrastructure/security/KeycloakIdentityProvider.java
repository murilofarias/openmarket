package com.example.openmarket.infrastructure.security;

import com.example.openmarket.application.port.AuthResult;
import com.example.openmarket.application.port.IdentityProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import jakarta.ws.rs.core.Response;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

@Component
@Primary
public class KeycloakIdentityProvider implements IdentityProvider {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${keycloak.server-url}")
    private String serverUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.client-id:openmarket-api}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    private Keycloak keycloak;
    private RealmResource realmResource;
    private String tokenUrl;
    private String logoutUrl;

    public KeycloakIdentityProvider(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        this.keycloak = KeycloakBuilder.builder()
            .serverUrl(serverUrl)
            .realm(realm)
            .clientId(clientId)
            .clientSecret(clientSecret)
            .grantType(org.keycloak.OAuth2Constants.CLIENT_CREDENTIALS)
            .build();

        this.realmResource = keycloak.realm(realm);
        this.tokenUrl = serverUrl + "/realms/" + realm + "/protocol/openid-connect/token";
        this.logoutUrl = serverUrl + "/realms/" + realm + "/protocol/openid-connect/logout";
    }

    @PreDestroy
    public void cleanup() {
        if (keycloak != null) {
            keycloak.close();
        }
    }

    @Override
    public String createUser(String email, String password, String name) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(email);
        user.setEmail(email);
        user.setEmailVerified(true);
        user.setEnabled(true);

        // Split name into first and last name
        String[] nameParts = name.split(" ", 2);
        user.setFirstName(nameParts[0]);
        if (nameParts.length > 1) {
            user.setLastName(nameParts[1]);
        }

        // Set password credentials before creation
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        credential.setTemporary(false);
        user.setCredentials(Collections.singletonList(credential));

        // Create user with credentials in a single request
        Response response = realmResource.users().create(user);

        if (response.getStatus() != 201) {
            throw new RuntimeException("Failed to create user: " + response.getStatusInfo());
        }

        // Extract user ID from location header
        String locationHeader = response.getHeaderString("Location");
        String userId = locationHeader.substring(locationHeader.lastIndexOf('/') + 1);

        response.close();

        return userId;
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
        UserResource userResource = realmResource.users().get(userId);

        for (String roleName : roles) {
            RoleRepresentation role = realmResource.roles().get(roleName).toRepresentation();
            userResource.roles().realmLevel().add(Collections.singletonList(role));
        }
    }

    @Override
    public void addRole(String userId, String role) {
        UserResource userResource = realmResource.users().get(userId);
        RoleRepresentation roleRepresentation = realmResource.roles().get(role).toRepresentation();
        userResource.roles().realmLevel().add(Collections.singletonList(roleRepresentation));
    }

    @Override
    public void removeRole(String userId, String role) {
        UserResource userResource = realmResource.users().get(userId);
        RoleRepresentation roleRepresentation = realmResource.roles().get(role).toRepresentation();
        userResource.roles().realmLevel().remove(Collections.singletonList(roleRepresentation));
    }

    @Override
    public boolean userExists(String userId) {
        try {
            realmResource.users().get(userId).toRepresentation();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void enableUser(String userId) {
        UserResource userResource = realmResource.users().get(userId);
        UserRepresentation user = userResource.toRepresentation();
        user.setEnabled(true);
        userResource.update(user);
    }

    @Override
    public void disableUser(String userId) {
        UserResource userResource = realmResource.users().get(userId);
        UserRepresentation user = userResource.toRepresentation();
        user.setEnabled(false);
        userResource.update(user);
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
}
