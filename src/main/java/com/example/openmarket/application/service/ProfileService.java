package com.example.openmarket.application.service;

import com.example.openmarket.application.domain.BuyerProfile;
import com.example.openmarket.application.domain.SellerProfile;
import com.example.openmarket.application.domain.SellerStatus;
import com.example.openmarket.application.exception.DomainException;
import com.example.openmarket.application.exception.ResourceNotFoundException;
import com.example.openmarket.application.port.BuyerProfileRepository;
import com.example.openmarket.application.port.IdentityProvider;
import com.example.openmarket.application.port.SellerProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ProfileService {

    private final BuyerProfileRepository buyerRepository;
    private final SellerProfileRepository sellerRepository;
    private final IdentityProvider identityProvider;

    public ProfileService(BuyerProfileRepository buyerRepository,
                         SellerProfileRepository sellerRepository,
                         IdentityProvider identityProvider) {
        this.buyerRepository = buyerRepository;
        this.sellerRepository = sellerRepository;
        this.identityProvider = identityProvider;
    }

    /**
     * Creates a buyer profile for an existing user in the identity provider.
     */
    public UUID createBuyerProfile(String userId, String defaultShippingAddress) {
        // Verify user exists in identity provider
        if (!identityProvider.userExists(userId)) {
            throw new ResourceNotFoundException("User", userId);
        }

        // Check if user already has a buyer profile
        if (buyerRepository.existsByUserId(userId)) {
            throw new DomainException("User already has a buyer profile");
        }

        // Create buyer profile
        BuyerProfile profile = BuyerProfile.create(userId, defaultShippingAddress);

        // Assign BUYER role in identity provider
        identityProvider.addRole(userId, "BUYER");

        // Save profile
        BuyerProfile saved = buyerRepository.save(profile);

        return saved.getId();
    }

    /**
     * Creates a seller profile for an existing user in the identity provider.
     */
    public UUID createSellerProfile(String userId, String storeName, String storeDescription) {
        // Verify user exists in identity provider
        if (!identityProvider.userExists(userId)) {
            throw new ResourceNotFoundException("User", userId);
        }

        // Check if user already has a seller profile
        if (sellerRepository.existsByUserId(userId)) {
            throw new DomainException("User already has a seller profile");
        }

        // Create seller profile
        SellerProfile profile = SellerProfile.create(userId, storeName, storeDescription);

        // Assign SELLER role in identity provider
        identityProvider.addRole(userId, "SELLER");

        // Save profile
        SellerProfile saved = sellerRepository.save(profile);

        return saved.getId();
    }

    /**
     * Finds buyer profile by userId.
     */
    public BuyerProfile findBuyerByUserId(String userId) {
        return buyerRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("BuyerProfile", userId));
    }

    /**
     * Finds seller profile by userId.
     */
    public SellerProfile findSellerByUserId(String userId) {
        return sellerRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("SellerProfile", userId));
    }

    /**
     * Finds buyer profile by id.
     */
    public BuyerProfile findBuyerById(UUID id) {
        return buyerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("BuyerProfile", id.toString()));
    }

    /**
     * Finds seller profile by id.
     */
    public SellerProfile findSellerById(UUID id) {
        return sellerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("SellerProfile", id.toString()));
    }

    /**
     * Updates buyer shipping address.
     */
    public void updateBuyerShippingAddress(String userId, String newAddress) {
        BuyerProfile profile = findBuyerByUserId(userId);
        profile.updateShippingAddress(newAddress);
        buyerRepository.save(profile);
    }

    /**
     * Updates seller store information.
     */
    public void updateSellerStoreInfo(String userId, String storeName, String storeDescription) {
        SellerProfile profile = findSellerByUserId(userId);
        profile.updateStoreInfo(storeName, storeDescription);
        sellerRepository.save(profile);
    }

    /**
     * Approves a seller profile (admin operation).
     */
    public void approveSeller(UUID sellerId) {
        SellerProfile profile = findSellerById(sellerId);
        profile.approve();
        sellerRepository.save(profile);
    }

    /**
     * Suspends a seller profile (admin operation).
     */
    public void suspendSeller(UUID sellerId, String reason) {
        SellerProfile profile = findSellerById(sellerId);
        profile.suspend(reason);

        // Optionally disable user in identity provider
        identityProvider.disableUser(profile.getUserId());

        sellerRepository.save(profile);
    }

    /**
     * Reactivates a suspended seller (admin operation).
     */
    public void reactivateSeller(UUID sellerId) {
        SellerProfile profile = findSellerById(sellerId);
        profile.reactivate();

        // Re-enable user in identity provider
        identityProvider.enableUser(profile.getUserId());

        sellerRepository.save(profile);
    }

    /**
     * Finds all sellers with a specific status (admin operation).
     */
    public List<SellerProfile> findSellersByStatus(SellerStatus status) {
        return sellerRepository.findByStatus(status);
    }
}
