package com.example.openmarket.controller;

import com.example.openmarket.application.service.AuthenticationService;
import com.example.openmarket.controller.dto.request.LoginRequest;
import com.example.openmarket.controller.dto.request.RefreshTokenRequest;
import com.example.openmarket.controller.dto.request.RegisterUserRequest;
import com.example.openmarket.controller.dto.response.LoginResponse;
import com.example.openmarket.controller.dto.response.RegisterUserResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    /**
     * Register a new user
     * POST /auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<RegisterUserResponse> register(
            @Valid @RequestBody RegisterUserRequest request) {

        String userId = authenticationService.registerUser(
            request.getEmail(),
            request.getPassword(),
            request.getName()
        );

        RegisterUserResponse response = new RegisterUserResponse(
            userId,
            "User registered successfully. Please login to get your access token."
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Login and get access token
     * POST /auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        LoginResponse response = authenticationService.login(
            request.getEmail(),
            request.getPassword()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Refresh access token using refresh token
     * POST /auth/refresh
     */
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {

        LoginResponse response = authenticationService.refreshToken(
            request.getRefreshToken()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Logout and invalidate refresh token
     * POST /auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @Valid @RequestBody RefreshTokenRequest request) {

        authenticationService.logout(request.getRefreshToken());

        return ResponseEntity.noContent().build();
    }
}
