package com.example.openmarket.infrastructure.persistence.entity;

import com.example.openmarket.application.domain.Role;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "profiles")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "role", discriminatorType = DiscriminatorType.STRING)
public abstract class ProfileEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private AccountEntity account;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, insertable = false, updatable = false)
    private Role role;

    @Column(name = "rating")
    private Double rating;

    public ProfileEntity() {}

    public ProfileEntity(Role role) {
        this.role = role;
    }

    // Getters/Setters
    public UUID getId() { return id; }

    public AccountEntity getAccount() { return account; }
    public Role getRole() { return role; }

    public void setAccount(AccountEntity account) {
        this.account = account;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }
}
