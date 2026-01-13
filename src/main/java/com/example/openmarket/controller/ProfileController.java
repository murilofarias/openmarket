package com.example.openmarket.controller;

import com.example.openmarket.application.domain.AuthenticatedUser;
import com.example.openmarket.application.domain.BuyerProfile;
import com.example.openmarket.application.domain.SellerProfile;
import com.example.openmarket.application.service.ProfileService;
import com.example.openmarket.controller.dto.request.CreateBuyerRequest;
import com.example.openmarket.controller.dto.request.CreateSellerRequest;
import com.example.openmarket.controller.dto.response.BuyerProfileDto;
import com.example.openmarket.controller.dto.response.CreateProfileResponse;
import com.example.openmarket.controller.dto.response.SellerProfileDto;
import com.example.openmarket.infrastructure.security.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/profiles")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    /**
     * Create buyer profile for the authenticated user
     * POST /profiles/buyer
     */
    @PostMapping("/buyer")
    public ResponseEntity<CreateProfileResponse> createBuyerProfile(
            @Valid @RequestBody CreateBuyerRequest request,
            @CurrentUser AuthenticatedUser user) {

        UUID profileId = profileService.createBuyerProfile(
            user.getUserId(),
            request.getDefaultShippingAddress()
        );

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(new CreateProfileResponse(profileId));
    }

    /**
     * Create seller profile for the authenticated user
     * POST /profiles/seller
     */
    @PostMapping("/seller")
    public ResponseEntity<CreateProfileResponse> createSellerProfile(
            @Valid @RequestBody CreateSellerRequest request,
            @CurrentUser AuthenticatedUser user) {

        UUID profileId = profileService.createSellerProfile(
            user.getUserId(),
            request.getStoreName(),
            request.getStoreDescription()
        );

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(new CreateProfileResponse(profileId));
    }

    /**
     * Get authenticated user's buyer profile
     * GET /profiles/buyer/me
     */
    @GetMapping("/buyer/me")
    public ResponseEntity<BuyerProfileDto> getMyBuyerProfile(
            @CurrentUser AuthenticatedUser user) {

        BuyerProfile profile = profileService.findBuyerByUserId(user.getUserId());

        BuyerProfileDto dto = new BuyerProfileDto(
            profile.getId(),
            user.getEmail(),
            user.getName(),
            profile.getDefaultShippingAddress(),
            profile.getTotalOrders(),
            profile.getRating(),
            profile.getCreatedAt()
        );

        return ResponseEntity.ok(dto);
    }

    /**
     * Get authenticated user's seller profile
     * GET /profiles/seller/me
     */
    @GetMapping("/seller/me")
    public ResponseEntity<SellerProfileDto> getMySellerProfile(
            @CurrentUser AuthenticatedUser user) {

        SellerProfile profile = profileService.findSellerByUserId(user.getUserId());

        SellerProfileDto dto = new SellerProfileDto(
            profile.getId(),
            user.getEmail(),
            user.getName(),
            profile.getStoreName(),
            profile.getStoreDescription(),
            profile.getStatus().name(),
            profile.getRating(),
            profile.getCreatedAt()
        );

        return ResponseEntity.ok(dto);
    }

    /**
     * Get any buyer profile by ID (public)
     * GET /profiles/buyer/{id}
     */
    @GetMapping("/buyer/{id}")
    public ResponseEntity<BuyerProfileDto> getBuyerProfile(@PathVariable UUID id) {
        BuyerProfile profile = profileService.findBuyerById(id);

        // Note: email and name are null since we don't have JWT for other users
        BuyerProfileDto dto = new BuyerProfileDto(
            profile.getId(),
            null,  // Email hidden for privacy
            null,  // Name hidden for privacy
            profile.getDefaultShippingAddress(),
            profile.getTotalOrders(),
            profile.getRating(),
            profile.getCreatedAt()
        );

        return ResponseEntity.ok(dto);
    }

    /**
     * Get any seller profile by ID (public)
     * GET /profiles/seller/{id}
     */
    @GetMapping("/seller/{id}")
    public ResponseEntity<SellerProfileDto> getSellerProfile(@PathVariable UUID id) {
        SellerProfile profile = profileService.findSellerById(id);

        // Note: email and name are null since we don't have JWT for other users
        SellerProfileDto dto = new SellerProfileDto(
            profile.getId(),
            null,  // Email hidden for privacy
            null,  // Name hidden for privacy
            profile.getStoreName(),
            profile.getStoreDescription(),
            profile.getStatus().name(),
            profile.getRating(),
            profile.getCreatedAt()
        );

        return ResponseEntity.ok(dto);
    }

    /**
     * Update buyer shipping address
     * PATCH /profiles/buyer/shipping-address
     */
    @PatchMapping("/buyer/shipping-address")
    public ResponseEntity<Void> updateShippingAddress(
            @RequestBody String newAddress,
            @CurrentUser AuthenticatedUser user) {

        profileService.updateBuyerShippingAddress(user.getUserId(), newAddress);

        return ResponseEntity.noContent().build();
    }

    /**
     * Update seller store information
     * PATCH /profiles/seller/store-info
     */
    @PatchMapping("/seller/store-info")
    public ResponseEntity<Void> updateStoreInfo(
            @Valid @RequestBody CreateSellerRequest request,
            @CurrentUser AuthenticatedUser user) {

        profileService.updateSellerStoreInfo(
            user.getUserId(),
            request.getStoreName(),
            request.getStoreDescription()
        );

        return ResponseEntity.noContent().build();
    }
}
