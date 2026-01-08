package com.example.openmarket.controller.dto.request;

import com.example.openmarket.application.command.CreateAccountCommand;
import com.example.openmarket.application.command.CreateBuyerProfileCommand;
import com.example.openmarket.application.command.CreateProfileCommand;
import com.example.openmarket.application.command.CreateSellerProfileCommand;
import com.example.openmarket.controller.validation.AtLeastOneProfile;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.HashSet;
import java.util.Set;

public class CreateAccountRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @Valid
    private CreateBuyerProfileRequest buyerProfile;

    @Valid
    private CreateSellerProfileRequest sellerProfile;

    public CreateAccountRequest() {}

    public CreateAccountRequest(String email, String password, String name,
                                CreateBuyerProfileRequest buyerProfile,
                                CreateSellerProfileRequest sellerProfile) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.buyerProfile = buyerProfile;
        this.sellerProfile = sellerProfile;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public CreateBuyerProfileRequest getBuyerProfile() { return buyerProfile; }
    public void setBuyerProfile(CreateBuyerProfileRequest buyerProfile) {
        this.buyerProfile = buyerProfile;
    }

    public CreateSellerProfileRequest getSellerProfile() { return sellerProfile; }
    public void setSellerProfile(CreateSellerProfileRequest sellerProfile) {
        this.sellerProfile = sellerProfile;
    }

    public CreateAccountCommand createCommand() {
        Set<CreateProfileCommand> profileCommands = new HashSet<>();

        if (buyerProfile != null) {
            profileCommands.add(new CreateBuyerProfileCommand(
                    buyerProfile.getDefaultShippingAddress()
            ));
        }

        if (sellerProfile != null) {
            profileCommands.add(new CreateSellerProfileCommand(
                    sellerProfile.getStoreName(),
                    sellerProfile.getStoreDescription()
            ));
        }

        return new CreateAccountCommand(email, password, name, profileCommands);
    }
}
