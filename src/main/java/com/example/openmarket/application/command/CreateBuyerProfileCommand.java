package com.example.openmarket.application.command;

import com.example.openmarket.application.domain.Buyer;
import com.example.openmarket.application.domain.Profile;
import com.example.openmarket.application.domain.Role;

public class CreateBuyerProfileCommand extends CreateProfileCommand {

    private final String defaultShippingAddress;

    public CreateBuyerProfileCommand(String defaultShippingAddress) {
        this.defaultShippingAddress = defaultShippingAddress;
    }

    public String getDefaultShippingAddress() {
        return defaultShippingAddress;
    }

    @Override
    public Role getRole() {
        return Role.BUYER;
    }

    @Override
    public Profile execute() {
        return Buyer.create(defaultShippingAddress);
    }
}
