package com.example.openmarket.application.port;

import com.example.openmarket.application.domain.Account;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepository {
    
    Account save(Account account);
    
    Optional<Account> findById(UUID id);
    
    Optional<Account> findByEmail(String email);
    
    void delete(UUID id);
    
    boolean existsByEmail(String email);
}