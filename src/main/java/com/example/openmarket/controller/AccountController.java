package com.example.openmarket.controller;


import com.example.openmarket.application.domain.Account;
import com.example.openmarket.application.domain.Buyer;
import com.example.openmarket.application.domain.Role;
import com.example.openmarket.application.domain.Seller;
import com.example.openmarket.application.service.AccountApplicationService;
import com.example.openmarket.controller.dto.request.CreateAccountRequest;

import com.example.openmarket.controller.dto.response.AccountResponse;
import com.example.openmarket.controller.dto.response.BuyerProfileResponse;
import com.example.openmarket.controller.dto.response.CreateAccountResponse;
import com.example.openmarket.controller.dto.response.SellerProfileResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.UUID;


@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountApplicationService accountService;

    public AccountController(AccountApplicationService accountService) {
        this.accountService = accountService;
    }

    @PostMapping()
    public ResponseEntity<CreateAccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest createAccountRequest) {
        UUID accountId = accountService.createAccount(createAccountRequest.createCommand());

        CreateAccountResponse response = new CreateAccountResponse(accountId);

        String location = "/accounts/" + accountId;

        return ResponseEntity.status(HttpStatus.CREATED)
                .header("Location", location)
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable UUID id) {
        Account account = accountService.findById(id);

        AccountResponse response = mapToAccountResponse(account);

        return ResponseEntity.ok(response);
    }

    private AccountResponse mapToAccountResponse(Account account) {
        BuyerProfileResponse buyerProfile = null;
        SellerProfileResponse sellerProfile = null;

        if (account.hasRole(Role.BUYER)) {
            Buyer buyer = (Buyer) account.getProfileByRole(Role.BUYER);
            buyerProfile = new BuyerProfileResponse(
                    buyer.getDefaultShippingAddress(),
                    buyer.getTotalOrders(),
                    buyer.getRating(),
                    buyer.getCreatedAt()
            );
        }

        if (account.hasRole(Role.SELLER)) {
            Seller seller = (Seller) account.getProfileByRole(Role.SELLER);
            sellerProfile = new SellerProfileResponse(
                    seller.getSellerStatus().name(),
                    seller.getStoreName(),
                    seller.getStoreDescription(),
                    seller.getRating(),
                    seller.getCreatedAt()
            );
        }

        return new AccountResponse(
                account.getId(),
                account.getEmail(),
                account.getName(),
                buyerProfile,
                sellerProfile,
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }
}
