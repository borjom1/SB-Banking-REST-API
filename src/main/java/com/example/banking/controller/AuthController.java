package com.example.banking.controller;

import com.example.banking.dto.LoginRequest;
import com.example.banking.dto.RefreshRequest;
import com.example.banking.dto.RegisterRequest;
import com.example.banking.dto.TokenResponse;
import com.example.banking.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.*;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @ResponseStatus(CREATED)
    @PostMapping("/register")
    public void register(@Valid @RequestBody RegisterRequest body) {
        log.info("-> register()");
        userService.register(body);
    }

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

    @ResponseStatus(NO_CONTENT)
    @PatchMapping("/logout")
    public void logout(@Valid @RequestBody RefreshRequest body) {
        log.info("-> logout()");
        userService.logout(body.getRefreshToken());
    }

}