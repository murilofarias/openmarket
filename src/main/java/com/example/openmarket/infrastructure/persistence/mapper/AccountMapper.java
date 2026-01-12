package com.example.openmarket.infrastructure.persistence.mapper;


import com.example.openmarket.application.domain.Account;
import com.example.openmarket.application.domain.Profile;
import com.example.openmarket.infrastructure.persistence.entity.AccountEntity;
import com.example.openmarket.infrastructure.persistence.entity.ProfileEntity;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class AccountMapper {

    private final ProfileMapper profileMapper;

    public AccountMapper(ProfileMapper profileMapper) {
        this.profileMapper = profileMapper;
    }

    public AccountEntity toEntity(Account domain) {
        AccountEntity entity = new AccountEntity();
        entity.setId(domain.getId());
        entity.setEmail(domain.getEmail());
        entity.setPasswordHash(domain.getPasswordHash());
        entity.setName(domain.getName());
        // createdAt and updatedAt will be set automatically by JPA lifecycle callbacks

        Set<ProfileEntity> profileEntities = domain.getProfiles().stream()
                .map(profileMapper::toEntity)
                .collect(Collectors.toSet());

        // Set bidirectional relationship
        profileEntities.forEach(profile -> profile.setAccount(entity));
        entity.setProfiles(profileEntities);

        return entity;
    }

    public Account toDomain(AccountEntity entity) {
        Set<Profile> profiles = entity.getProfiles().stream()
                .map(profileMapper::toDomain)
                .collect(Collectors.toSet());

        return Account.reconstitute(
                entity.getId(),
                entity.getEmail(),
                entity.getPasswordHash(),
                entity.getName(),
                profiles,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
