package com.example.openmarket.controller.dto.response;

import java.util.UUID;

public class CreateProfileResponse {
    private final UUID profileId;

    public CreateProfileResponse(UUID profileId) {
        this.profileId = profileId;
    }

    public UUID getProfileId() {
        return profileId;
    }
}
