package com.example.openmarket.application.domain;

import com.example.openmarket.application.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Account Domain Tests")
class
AccountTest {

    @Nested
    @DisplayName("Account Creation")
    class AccountCreation {

        @Test
        @DisplayName("Should create account with valid data and single buyer profile")
        void shouldCreateAccountWithValidDataAndBuyerProfile() {
            // Arrange
            String email = "john.doe@example.com";
            String passwordHash = "hashed_password_123";
            String name = "John Doe";
            Set<Profile> profiles = new HashSet<>();
            profiles.add(Buyer.create("123 Main St"));

            // Act
            Account account = Account.create(email, passwordHash, name, profiles);

            // Assert
            assertNotNull(account);
            assertNotNull(account.getId());
            assertEquals(email, account.getEmail());
            assertEquals(passwordHash, account.getPasswordHash());
            assertEquals(name, account.getName());
            assertEquals(1, account.getProfiles().size());
            assertTrue(account.hasRole(Role.BUYER));
            // Note: createdAt and updatedAt will be null until persisted by JPA
        }

        @Test
        @DisplayName("Should create account with valid data and single seller profile")
        void shouldCreateAccountWithValidDataAndSellerProfile() {
            // Arrange
            String email = "seller@example.com";
            String passwordHash = "hashed_password_456";
            String name = "Jane Seller";
            Set<Profile> profiles = new HashSet<>();
            profiles.add(Seller.create("My Store", "Best products"));

            // Act
            Account account = Account.create(email, passwordHash, name, profiles);

            // Assert
            assertNotNull(account);
            assertTrue(account.hasRole(Role.SELLER));
            assertFalse(account.hasRole(Role.BUYER));
        }

        @Test
        @DisplayName("Should create account with both buyer and seller profiles")
        void shouldCreateAccountWithBothProfiles() {
            // Arrange
            String email = "both@example.com";
            String passwordHash = "hashed_password_789";
            String name = "Both User";
            Set<Profile> profiles = new HashSet<>();
            profiles.add(Buyer.create("456 Oak Ave"));
            profiles.add(Seller.create("Both Store", "Buying and selling"));

            // Act
            Account account = Account.create(email, passwordHash, name, profiles);

            // Assert
            assertNotNull(account);
            assertEquals(2, account.getProfiles().size());
            assertTrue(account.hasRole(Role.BUYER));
            assertTrue(account.hasRole(Role.SELLER));
        }

        @Test
        @DisplayName("Should throw exception when email is null")
        void shouldThrowExceptionWhenEmailIsNull() {
            // Arrange
            Set<Profile> profiles = new HashSet<>();
            profiles.add(Buyer.create("789 Elm St"));

            // Act & Assert
            DomainException exception = assertThrows(DomainException.class, () ->
                    Account.create(null, "password", "Name", profiles)
            );
            assertEquals("Invalid email format", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when email format is invalid - no @ symbol")
        void shouldThrowExceptionWhenEmailFormatIsInvalid() {
            // Arrange
            Set<Profile> profiles = new HashSet<>();
            profiles.add(Buyer.create("789 Elm St"));

            // Act & Assert
            DomainException exception = assertThrows(DomainException.class, () ->
                    Account.create("invalid-email", "password", "Name", profiles)
            );
            assertEquals("Invalid email format", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when email has no domain")
        void shouldThrowExceptionWhenEmailHasNoDomain() {
            // Arrange
            Set<Profile> profiles = new HashSet<>();
            profiles.add(Buyer.create("789 Elm St"));

            // Act & Assert
            DomainException exception = assertThrows(DomainException.class, () ->
                    Account.create("user@", "password", "Name", profiles)
            );
            assertEquals("Invalid email format", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when email starts with @")
        void shouldThrowExceptionWhenEmailStartsWithAt() {
            // Arrange
            Set<Profile> profiles = new HashSet<>();
            profiles.add(Buyer.create("789 Elm St"));

            // Act & Assert
            DomainException exception = assertThrows(DomainException.class, () ->
                    Account.create("@example.com", "password", "Name", profiles)
            );
            assertEquals("Invalid email format", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when email is empty string")
        void shouldThrowExceptionWhenEmailIsEmpty() {
            // Arrange
            Set<Profile> profiles = new HashSet<>();
            profiles.add(Buyer.create("789 Elm St"));

            // Act & Assert
            DomainException exception = assertThrows(DomainException.class, () ->
                    Account.create("", "password", "Name", profiles)
            );
            assertEquals("Invalid email format", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when email has spaces")
        void shouldThrowExceptionWhenEmailHasSpaces() {
            // Arrange
            Set<Profile> profiles = new HashSet<>();
            profiles.add(Buyer.create("789 Elm St"));

            // Act & Assert
            DomainException exception = assertThrows(DomainException.class, () ->
                    Account.create("user name@example.com", "password", "Name", profiles)
            );
            assertEquals("Invalid email format", exception.getMessage());
        }

        @Test
        @DisplayName("Should accept valid email with numbers and special characters")
        void shouldAcceptValidEmailWithNumbersAndSpecialCharacters() {
            // Arrange
            String email = "user.name+tag123@example.co.uk";
            Set<Profile> profiles = new HashSet<>();
            profiles.add(Buyer.create("789 Elm St"));

            // Act
            Account account = Account.create(email, "password", "Name", profiles);

            // Assert
            assertNotNull(account);
            assertEquals(email, account.getEmail());
        }

        @Test
        @DisplayName("Should accept valid email with underscore and dash")
        void shouldAcceptValidEmailWithUnderscoreAndDash() {
            // Arrange
            String email = "user_name-test@my-domain.com";
            Set<Profile> profiles = new HashSet<>();
            profiles.add(Buyer.create("789 Elm St"));

            // Act
            Account account = Account.create(email, "password", "Name", profiles);

            // Assert
            assertNotNull(account);
            assertEquals(email, account.getEmail());
        }

        @Test
        @DisplayName("Should throw exception when name is null")
        void shouldThrowExceptionWhenNameIsNull() {
            // Arrange
            Set<Profile> profiles = new HashSet<>();
            profiles.add(Buyer.create("789 Elm St"));

            // Act & Assert
            DomainException exception = assertThrows(DomainException.class, () ->
                    Account.create("valid@example.com", "password", null, profiles)
            );
            assertEquals("Name cannot be empty", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when name is empty string")
        void shouldThrowExceptionWhenNameIsEmptyString() {
            // Arrange
            Set<Profile> profiles = new HashSet<>();
            profiles.add(Buyer.create("789 Elm St"));

            // Act & Assert
            DomainException exception = assertThrows(DomainException.class, () ->
                    Account.create("valid@example.com", "password", "", profiles)
            );
            assertEquals("Name cannot be empty", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when name contains only spaces")
        void shouldThrowExceptionWhenNameIsOnlySpaces() {
            // Arrange
            Set<Profile> profiles = new HashSet<>();
            profiles.add(Buyer.create("789 Elm St"));

            // Act & Assert
            DomainException exception = assertThrows(DomainException.class, () ->
                    Account.create("valid@example.com", "password", "  ", profiles)
            );
            assertEquals("Name cannot be empty", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when name contains only tabs")
        void shouldThrowExceptionWhenNameIsOnlyTabs() {
            // Arrange
            Set<Profile> profiles = new HashSet<>();
            profiles.add(Buyer.create("789 Elm St"));

            // Act & Assert
            DomainException exception = assertThrows(DomainException.class, () ->
                    Account.create("valid@example.com", "password", "\t\t", profiles)
            );
            assertEquals("Name cannot be empty", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when name contains mixed whitespace characters")
        void shouldThrowExceptionWhenNameIsMixedWhitespace() {
            // Arrange
            Set<Profile> profiles = new HashSet<>();
            profiles.add(Buyer.create("789 Elm St"));

            // Act & Assert
            DomainException exception = assertThrows(DomainException.class, () ->
                    Account.create("valid@example.com", "password", " \t \n ", profiles)
            );
            assertEquals("Name cannot be empty", exception.getMessage());
        }

        @Test
        @DisplayName("Should accept name with valid characters and spaces")
        void shouldAcceptNameWithValidCharactersAndSpaces() {
            // Arrange
            String name = "John Doe Jr.";
            Set<Profile> profiles = new HashSet<>();
            profiles.add(Buyer.create("789 Elm St"));

            // Act
            Account account = Account.create("valid@example.com", "password", name, profiles);

            // Assert
            assertNotNull(account);
            assertEquals(name, account.getName());
        }

        @Test
        @DisplayName("Should accept name with special characters")
        void shouldAcceptNameWithSpecialCharacters() {
            // Arrange
            String name = "José María O'Connor-Smith";
            Set<Profile> profiles = new HashSet<>();
            profiles.add(Buyer.create("789 Elm St"));

            // Act
            Account account = Account.create("valid@example.com", "password", name, profiles);

            // Assert
            assertNotNull(account);
            assertEquals(name, account.getName());
        }

        @Test
        @DisplayName("Should throw exception when profiles set is null")
        void shouldThrowExceptionWhenProfilesIsNull() {
            // Act & Assert
            DomainException exception = assertThrows(DomainException.class, () ->
                    Account.create("valid@example.com", "password", "Name", null)
            );
            assertEquals("At least one profile (buyer or seller) must be provided", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when profiles set is empty")
        void shouldThrowExceptionWhenProfilesIsEmpty() {
            // Arrange
            Set<Profile> profiles = new HashSet<>();

            // Act & Assert
            DomainException exception = assertThrows(DomainException.class, () ->
                    Account.create("valid@example.com", "password", "Name", profiles)
            );
            assertEquals("At least one profile (buyer or seller) must be provided", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when multiple buyer profiles are provided")
        void shouldThrowExceptionWhenMultipleBuyerProfiles() {
            // Arrange
            Set<Profile> profiles = new HashSet<>();
            profiles.add(Buyer.create("Address 1"));
            profiles.add(Buyer.create("Address 2"));

            // Act & Assert
            DomainException exception = assertThrows(DomainException.class, () ->
                    Account.create("valid@example.com", "password", "Name", profiles)
            );
            assertEquals("Only one buyer profile is allowed", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when multiple seller profiles are provided")
        void shouldThrowExceptionWhenMultipleSellerProfiles() {
            // Arrange
            Set<Profile> profiles = new HashSet<>();
            profiles.add(Seller.create("Store 1", "Description 1"));
            profiles.add(Seller.create("Store 2", "Description 2"));

            // Act & Assert
            DomainException exception = assertThrows(DomainException.class, () ->
                    Account.create("valid@example.com", "password", "Name", profiles)
            );
            assertEquals("Only one seller profile is allowed", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when multiple buyers and one seller are provided")
        void shouldThrowExceptionWhenMultipleBuyersAndOneSeller() {
            // Arrange
            Set<Profile> profiles = new HashSet<>();
            profiles.add(Buyer.create("Address 1"));
            profiles.add(Buyer.create("Address 2"));
            profiles.add(Seller.create("Store", "Description"));

            // Act & Assert
            DomainException exception = assertThrows(DomainException.class, () ->
                    Account.create("valid@example.com", "password", "Name", profiles)
            );
            assertEquals("Only one buyer profile is allowed", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when one buyer and multiple sellers are provided")
        void shouldThrowExceptionWhenOneBuyerAndMultipleSellers() {
            // Arrange
            Set<Profile> profiles = new HashSet<>();
            profiles.add(Buyer.create("Address"));
            profiles.add(Seller.create("Store 1", "Description 1"));
            profiles.add(Seller.create("Store 2", "Description 2"));

            // Act & Assert
            DomainException exception = assertThrows(DomainException.class, () ->
                    Account.create("valid@example.com", "password", "Name", profiles)
            );
            assertEquals("Only one seller profile is allowed", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when multiple buyers and multiple sellers are provided")
        void shouldThrowExceptionWhenMultipleBuyersAndMultipleSellers() {
            // Arrange
            Set<Profile> profiles = new HashSet<>();
            profiles.add(Buyer.create("Address 1"));
            profiles.add(Buyer.create("Address 2"));
            profiles.add(Seller.create("Store 1", "Description 1"));
            profiles.add(Seller.create("Store 2", "Description 2"));

            // Act & Assert - Should fail on buyer validation first
            DomainException exception = assertThrows(DomainException.class, () ->
                    Account.create("valid@example.com", "password", "Name", profiles)
            );
            assertTrue(
                    exception.getMessage().equals("Only one buyer profile is allowed") ||
                    exception.getMessage().equals("Only one seller profile is allowed")
            );
        }
    }

    @Nested
    @DisplayName("Add Profile")
    class AddProfile {

        @Test
        @DisplayName("Should add seller profile to account that only has buyer profile")
        void shouldAddSellerProfileToAccountWithBuyer() {
            // Arrange
            Set<Profile> profiles = new HashSet<>();
            profiles.add(Buyer.create("123 Main St"));
            Account account = Account.create("user@example.com", "password", "User", profiles);

            // Act
            Seller seller = Seller.create("New Store", "Description");
            account.addProfile(seller);

            // Assert
            assertEquals(2, account.getProfiles().size());
            assertTrue(account.hasRole(Role.BUYER));
            assertTrue(account.hasRole(Role.SELLER));
        }

        @Test
        @DisplayName("Should add buyer profile to account that only has seller profile")
        void shouldAddBuyerProfileToAccountWithSeller() {
            // Arrange
            Set<Profile> profiles = new HashSet<>();
            profiles.add(Seller.create("Store", "Description"));
            Account account = Account.create("user@example.com", "password", "User", profiles);

            // Act
            Buyer buyer = Buyer.create("456 Oak Ave");
            account.addProfile(buyer);

            // Assert
            assertEquals(2, account.getProfiles().size());
            assertTrue(account.hasRole(Role.BUYER));
            assertTrue(account.hasRole(Role.SELLER));
        }

        @Test
        @DisplayName("Should throw exception when adding duplicate buyer profile")
        void shouldThrowExceptionWhenAddingDuplicateBuyerProfile() {
            // Arrange
            Set<Profile> profiles = new HashSet<>();
            profiles.add(Buyer.create("123 Main St"));
            Account account = Account.create("user@example.com", "password", "User", profiles);

            // Act & Assert
            Buyer anotherBuyer = Buyer.create("789 Elm St");
            DomainException exception = assertThrows(DomainException.class, () ->
                    account.addProfile(anotherBuyer)
            );
            assertEquals("Profile with role BUYER already exists", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when adding duplicate seller profile")
        void shouldThrowExceptionWhenAddingDuplicateSellerProfile() {
            // Arrange
            Set<Profile> profiles = new HashSet<>();
            profiles.add(Seller.create("Store 1", "Description"));
            Account account = Account.create("user@example.com", "password", "User", profiles);

            // Act & Assert
            Seller anotherSeller = Seller.create("Store 2", "Another description");
            DomainException exception = assertThrows(DomainException.class, () ->
                    account.addProfile(anotherSeller)
            );
            assertEquals("Profile with role SELLER already exists", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Get Profile By Role")
    class GetProfileByRole {

        @Test
        @DisplayName("Should return buyer profile when account has buyer role")
        void shouldReturnBuyerProfile() {
            // Arrange
            Set<Profile> profiles = new HashSet<>();
            Buyer buyer = Buyer.create("123 Main St");
            profiles.add(buyer);
            Account account = Account.create("user@example.com", "password", "User", profiles);

            // Act
            Profile retrievedProfile = account.getProfileByRole(Role.BUYER);

            // Assert
            assertNotNull(retrievedProfile);
            assertEquals(Role.BUYER, retrievedProfile.getProfileRole());
            assertTrue(retrievedProfile instanceof Buyer);
        }

        @Test
        @DisplayName("Should throw exception when profile with requested role does not exist")
        void shouldThrowExceptionWhenProfileNotFound() {
            // Arrange
            Set<Profile> profiles = new HashSet<>();
            profiles.add(Buyer.create("123 Main St"));
            Account account = Account.create("user@example.com", "password", "User", profiles);

            // Act & Assert
            IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                    account.getProfileByRole(Role.SELLER)
            );
            assertEquals("No profile with role: SELLER", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Update Name")
    class UpdateName {

        @Test
        @DisplayName("Should update account name with valid name")
        void shouldUpdateNameWithValidName() {
            // Arrange
            Set<Profile> profiles = new HashSet<>();
            profiles.add(Buyer.create("123 Main St"));
            Account account = Account.create("user@example.com", "password", "Old Name", profiles);
            String newName = "New Name";

            // Act
            account.updateName(newName);

            // Assert
            assertEquals(newName, account.getName());
        }

        @Test
        @DisplayName("Should throw exception when updating with null name")
        void shouldThrowExceptionWhenUpdatingWithNullName() {
            // Arrange
            Set<Profile> profiles = new HashSet<>();
            profiles.add(Buyer.create("123 Main St"));
            Account account = Account.create("user@example.com", "password", "Old Name", profiles);

            // Act & Assert
            DomainException exception = assertThrows(DomainException.class, () ->
                    account.updateName(null)
            );
            assertEquals("Name cannot be empty", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception when updating with empty name")
        void shouldThrowExceptionWhenUpdatingWithEmptyName() {
            // Arrange
            Set<Profile> profiles = new HashSet<>();
            profiles.add(Buyer.create("123 Main St"));
            Account account = Account.create("user@example.com", "password", "Old Name", profiles);

            // Act & Assert
            DomainException exception = assertThrows(DomainException.class, () ->
                    account.updateName("   ")
            );
            assertEquals("Name cannot be empty", exception.getMessage());
        }
    }
}
