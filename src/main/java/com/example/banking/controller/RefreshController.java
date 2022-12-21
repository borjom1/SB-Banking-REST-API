package com.example.banking.controller;

import com.example.banking.dto.RefreshRequest;
import com.example.banking.dto.TokenResponse;
import com.example.banking.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/refresh")
public class RefreshController {

    private final UserService userService;

    @Autowired
    public RefreshController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshRequest body) {
        try {
            var result = userService.getTokens(body.getRefreshToken());
            var response = TokenResponse.builder()
                    .accessToken(result.get("accessToken"))
                    .accessTokenExpiration(result.get("accessTokenExpiration"))
                    .refreshToken(result.get("refreshToken"))
                    .refreshTokenExpiration(result.get("refreshTokenExpiration"))
                    .build();

            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            return ResponseEntity.badRequest().body(
                    TokenResponse.builder()
                            .error(e.getMessage())
                            .build()
            );
        }
    }
}