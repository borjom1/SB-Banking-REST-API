package com.example.banking.controller;

import com.example.banking.dto.auth.LoginRequest;
import com.example.banking.dto.auth.RefreshRequest;
import com.example.banking.dto.auth.RegisterRequest;
import com.example.banking.dto.auth.TokenResponse;
import com.example.banking.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.*;

@Tag(
        name = "Authorization",
        description = "User back-end lifecycle (registration, login, logout, etc.)"
)
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @Operation(summary = "Register a new user account")
    @ResponseStatus(CREATED)
    @PostMapping("/register")
    public void register(@Valid @RequestBody RegisterRequest body) {
        log.info("-> register()");
        userService.register(body);
    }

    @Operation(summary = "Log in existing account")
    @ResponseStatus(OK)
    @PatchMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest body) {
        log.info("-> login() phone-number: {}, password: {}", body.getPhoneNumber(), body.getPassword());

        var tokens = userService.login(body);
        return TokenResponse.builder()
                .accessToken(tokens.access())
                .refreshToken(tokens.refresh())
                .build();
    }

    @Operation(
            summary = "Refresh access and refresh token",
            description = "Old refresh token will be unavailable to use"
    )
    @ResponseStatus(OK)
    @PatchMapping("/refresh")
    public TokenResponse refresh(@Valid @RequestBody RefreshRequest body) {
        log.info("-> refresh()");

        var tokens = userService.refreshTokens(body.getRefreshToken());
        return TokenResponse.builder()
                .accessToken(tokens.access())
                .refreshToken(tokens.refresh())
                .build();
    }

    @Operation(
            summary = "Log out from user account",
            description = "Existing refresh token will be deleted. Access token will be active till expiration date"
    )
    @ResponseStatus(NO_CONTENT)
    @PatchMapping("/logout")
    public void logout(@Valid @RequestBody RefreshRequest body) {
        log.info("-> logout()");
        userService.logout(body.getRefreshToken());
    }

}