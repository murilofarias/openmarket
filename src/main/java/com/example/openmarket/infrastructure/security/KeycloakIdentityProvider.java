package com.example.openmarket.infrastructure.security;

import com.example.openmarket.application.port.IdentityProvider;
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
import org.springframework.stereotype.Component;

import jakarta.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Component
@Primary
public class KeycloakIdentityProvider implements IdentityProvider {

    @Value("${keycloak.server-url}")
    private String serverUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.admin-username}")
    private String adminUsername;

    @Value("${keycloak.admin-password}")
    private String adminPassword;

    @Value("${keycloak.client-id:admin-cli}")
    private String clientId;

    private Keycloak keycloak;
    private RealmResource realmResource;

    @PostConstruct
    public void init() {
        this.keycloak = KeycloakBuilder.builder()
            .serverUrl(serverUrl)
            .realm("master")  // Admin client connects to master realm
            .clientId(clientId)
            .username(adminUsername)
            .password(adminPassword)
            .build();

        this.realmResource = keycloak.realm(realm);
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

        // Create user
        Response response = realmResource.users().create(user);

        if (response.getStatus() != 201) {
            throw new RuntimeException("Failed to create user: " + response.getStatusInfo());
        }

        // Extract user ID from location header
        String locationHeader = response.getHeaderString("Location");
        String userId = locationHeader.substring(locationHeader.lastIndexOf('/') + 1);

        response.close();

        // Set password
        UserResource userResource = realmResource.users().get(userId);
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        credential.setTemporary(false);
        userResource.resetPassword(credential);

        return userId;
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
}
