package com.example.openmarket.infrastructure.persistence.mapper;

import com.example.openmarket.application.domain.User;
import com.example.openmarket.infrastructure.persistence.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserEntity toEntity(User domain) {
        UserEntity entity = new UserEntity();
        entity.setId(domain.getId());
        entity.setExternalAuthId(domain.getExternalAuthId());
        entity.setEmail(domain.getEmail());
        entity.setName(domain.getName());
        // createdAt and updatedAt will be set automatically by JPA lifecycle callbacks
        return entity;
    }

    public User toDomain(UserEntity entity) {
        return User.reconstitute(
                entity.getId(),
                entity.getExternalAuthId(),
                entity.getEmail(),
                entity.getName(),
                entity.getCreatedAt()
        );
    }
}
