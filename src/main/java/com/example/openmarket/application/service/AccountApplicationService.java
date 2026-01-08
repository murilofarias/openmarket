package com.example.openmarket.application.service;

import com.example.openmarket.application.command.CreateAccountCommand;
import com.example.openmarket.application.domain.Account;
import com.example.openmarket.application.exception.EmailAlreadyAssociatedException;
import com.example.openmarket.application.port.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class AccountApplicationService {

    private final AccountRepository accountRepository;

    public AccountApplicationService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    /**
     * Creates a new account.
     */
    public UUID createAccount(CreateAccountCommand command) {
        if (accountRepository.existsByEmail(command.getEmail())) {
            throw new EmailAlreadyAssociatedException(command.getEmail());
        }

        Account account = command.execute();

        Account savedAccount = accountRepository.save(account);

        return savedAccount.getId();
    }

    /**
     * Finds an account by its ID.
     */
    public Account findById(UUID id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Account not found with id: " + id));
    }
}
