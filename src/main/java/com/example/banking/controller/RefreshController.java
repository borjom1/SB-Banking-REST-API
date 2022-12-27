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

import java.util.HashMap;
import java.util.Map;

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

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@Valid @RequestBody RefreshRequest body) {
        log.info("IN RefreshController -> logout()");
        Map<String, String> result = new HashMap<>();
        try {
            userService.logout(body.getRefreshToken());
            result.put("message", "success");
            return ResponseEntity.ok(result);
        } catch (BadCredentialsException e) {
            result.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

}