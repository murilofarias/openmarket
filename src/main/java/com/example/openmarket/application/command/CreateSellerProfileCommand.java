package com.example.openmarket.application.command;

import com.example.openmarket.application.domain.Profile;
import com.example.openmarket.application.domain.Role;
import com.example.openmarket.application.domain.Seller;

public class CreateSellerProfileCommand extends CreateProfileCommand {

    private final String storeName;
    private final String storeDescription;

    public CreateSellerProfileCommand(String storeName, String storeDescription) {
        this.storeName = storeName;
        this.storeDescription = storeDescription;
    }

    public String getStoreName() {
        return storeName;
    }

    public String getStoreDescription() {
        return storeDescription;
    }

    @Override
    public Role getRole() {
        return Role.SELLER;
    }

    @Override
    public Profile execute() {
        return Seller.create(storeName, storeDescription);
    }
}
