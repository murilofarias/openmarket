package com.example.openmarket.application.service;

import com.example.openmarket.application.command.CreateAccountCommand;
import com.example.openmarket.application.command.CreateBuyerProfileCommand;
import com.example.openmarket.application.command.CreateProfileCommand;
import com.example.openmarket.application.command.CreateSellerProfileCommand;
import com.example.openmarket.application.domain.Account;
import com.example.openmarket.application.domain.Buyer;
import com.example.openmarket.application.domain.Profile;
import com.example.openmarket.application.domain.Seller;
import com.example.openmarket.application.exception.EmailAlreadyAssociatedException;
import com.example.openmarket.application.exception.ResourceNotFoundException;
import com.example.openmarket.application.port.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountApplicationService Tests")
class AccountApplicationServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountApplicationService accountApplicationService;

    @Nested
    @DisplayName("Create Account")
    class CreateAccount {

        @Test
        @DisplayName("Should create account successfully with buyer profile")
        void shouldCreateAccountSuccessfullyWithBuyerProfile() {
            // Arrange
            String email = "newuser@example.com";
            String password = "password123";
            String name = "New User";
            String shippingAddress = "123 Main St";

            Set<CreateProfileCommand> profileCommands = new HashSet<>();
            profileCommands.add(new CreateBuyerProfileCommand(shippingAddress));

            CreateAccountCommand command = new CreateAccountCommand(
                    email,
                    password,
                    name,
                    profileCommands
            );

            // Mock repository behavior
            when(accountRepository.existsByEmail(email)).thenReturn(false);

            // Create a mock account with a specific ID that will be returned by save
            UUID expectedId = UUID.randomUUID();
            Set<Profile> profiles = new HashSet<>();
            profiles.add(Buyer.create(shippingAddress));

            when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> {
                return Account.reconstitute(expectedId, email, password, name, profiles, LocalDateTime.now(), LocalDateTime.now());
                // In real scenario, the repository would set the ID
            });

            // Act
            UUID accountId = accountApplicationService.createAccount(command);

            // Assert
            assertNotNull(accountId);
            assertEquals(expectedId, accountId);
            verify(accountRepository, times(1)).existsByEmail(email);
            verify(accountRepository, times(1)).save(any(Account.class));
        }

        @Test
        @DisplayName("Should create account successfully with seller profile")
        void shouldCreateAccountSuccessfullyWithSellerProfile() {
            // Arrange
            String email = "seller@example.com";
            String password = "password456";
            String name = "Seller User";
            String storeName = "My Store";
            String storeDescription = "Best products";

            Set<CreateProfileCommand> profileCommands = new HashSet<>();
            profileCommands.add(new CreateSellerProfileCommand(storeName, storeDescription));

            CreateAccountCommand command = new CreateAccountCommand(
                    email,
                    password,
                    name,
                    profileCommands
            );

            // Mock repository behavior
            when(accountRepository.existsByEmail(email)).thenReturn(false);

            Set<Profile> profiles = new HashSet<>();
            profiles.add(Seller.create(storeName, storeDescription));
            Account savedAccount = Account.create(email, password, name, profiles);

            when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);

            // Act
            UUID accountId = accountApplicationService.createAccount(command);

            // Assert
            assertNotNull(accountId);
            verify(accountRepository, times(1)).existsByEmail(email);
            verify(accountRepository, times(1)).save(any(Account.class));
        }

        @Test
        @DisplayName("Should create account successfully with both buyer and seller profiles")
        void shouldCreateAccountSuccessfullyWithBothProfiles() {
            // Arrange
            String email = "both@example.com";
            String password = "password789";
            String name = "Both User";
            String shippingAddress = "456 Oak Ave";
            String storeName = "Both Store";
            String storeDescription = "Buying and selling";

            Set<CreateProfileCommand> profileCommands = new HashSet<>();
            profileCommands.add(new CreateBuyerProfileCommand(shippingAddress));
            profileCommands.add(new CreateSellerProfileCommand(storeName, storeDescription));

            CreateAccountCommand command = new CreateAccountCommand(
                    email,
                    password,
                    name,
                    profileCommands
            );

            // Mock repository behavior
            when(accountRepository.existsByEmail(email)).thenReturn(false);

            Set<Profile> profiles = new HashSet<>();
            profiles.add(Buyer.create(shippingAddress));
            profiles.add(Seller.create(storeName, storeDescription));
            Account savedAccount = Account.create(email, password, name, profiles);

            when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);

            // Act
            UUID accountId = accountApplicationService.createAccount(command);

            // Assert
            assertNotNull(accountId);
            verify(accountRepository, times(1)).existsByEmail(email);
            verify(accountRepository, times(1)).save(any(Account.class));
        }

        @Test
        @DisplayName("Should throw EmailAlreadyAssociatedException when email already exists")
        void shouldThrowExceptionWhenEmailAlreadyExists() {
            // Arrange
            String email = "existing@example.com";
            String password = "password123";
            String name = "User";

            Set<CreateProfileCommand> profileCommands = new HashSet<>();
            profileCommands.add(new CreateBuyerProfileCommand("123 Main St"));

            CreateAccountCommand command = new CreateAccountCommand(
                    email,
                    password,
                    name,
                    profileCommands
            );

            // Mock repository to return true for existsByEmail
            when(accountRepository.existsByEmail(email)).thenReturn(true);

            // Act & Assert
            EmailAlreadyAssociatedException exception = assertThrows(
                    EmailAlreadyAssociatedException.class,
                    () -> accountApplicationService.createAccount(command)
            );

            assertTrue(exception.getMessage().contains(email));
            verify(accountRepository, times(1)).existsByEmail(email);
            verify(accountRepository, never()).save(any(Account.class));
        }

        @Test
        @DisplayName("Should validate account data through domain when creating")
        void shouldValidateAccountDataThroughDomain() {
            // Arrange - Creating command with invalid email (validation happens in domain)
            String invalidEmail = "invalid-email";
            String password = "password123";
            String name = "User";

            Set<CreateProfileCommand> profileCommands = new HashSet<>();
            profileCommands.add(new CreateBuyerProfileCommand("123 Main St"));

            CreateAccountCommand command = new CreateAccountCommand(
                    invalidEmail,
                    password,
                    name,
                    profileCommands
            );

            // Mock repository behavior
            when(accountRepository.existsByEmail(invalidEmail)).thenReturn(false);

            // Act & Assert - Domain validation should throw exception
            assertThrows(Exception.class, () ->
                    accountApplicationService.createAccount(command)
            );

            verify(accountRepository, times(1)).existsByEmail(invalidEmail);
            verify(accountRepository, never()).save(any(Account.class));
        }
    }

    @Nested
    @DisplayName("Find Account By Id")
    class FindAccountById {

        @Test
        @DisplayName("Should find account by id successfully")
        void shouldFindAccountByIdSuccessfully() {
            // Arrange
            UUID accountId = UUID.randomUUID();
            Set<Profile> profiles = new HashSet<>();
            profiles.add(Buyer.create("123 Main St"));
            Account mockAccount = Account.create(
                    "user@example.com",
                    "password",
                    "User",
                    profiles
            );

            when(accountRepository.findById(accountId)).thenReturn(Optional.of(mockAccount));

            // Act
            Account foundAccount = accountApplicationService.findById(accountId);

            // Assert
            assertNotNull(foundAccount);
            assertEquals(mockAccount.getEmail(), foundAccount.getEmail());
            assertEquals(mockAccount.getName(), foundAccount.getName());
            verify(accountRepository, times(1)).findById(accountId);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when account not found")
        void shouldThrowExceptionWhenAccountNotFound() {
            // Arrange
            UUID nonExistentId = UUID.randomUUID();
            when(accountRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // Act & Assert
            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> accountApplicationService.findById(nonExistentId)
            );

            assertTrue(exception.getMessage().contains("No Account was found"));
            assertTrue(exception.getMessage().contains(nonExistentId.toString()));
            verify(accountRepository, times(1)).findById(nonExistentId);
        }
    }
}
