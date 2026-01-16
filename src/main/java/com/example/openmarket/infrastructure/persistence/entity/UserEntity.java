package com.example.openmarket.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "users",
    indexes = {
        @Index(name = "idx_users_external_auth_id", columnList = "external_auth_id", unique = true),
        @Index(name = "idx_users_email", columnList = "email", unique = true)
    }
)
public class UserEntity extends BaseEntity {

    @Id
    private UUID id;

    @Column(name = "external_auth_id", unique = true, nullable = false)
    private String externalAuthId;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    public UserEntity() {}

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getExternalAuthId() {
        return externalAuthId;
    }

    public void setExternalAuthId(String externalAuthId) {
        this.externalAuthId = externalAuthId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
