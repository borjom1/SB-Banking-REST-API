package com.example.banking.controller;

import com.example.banking.dto.LoginRequest;
import com.example.banking.dto.TokenResponse;
import com.example.banking.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/login")
public class LoginController {

    private final UserService userService;

    @Autowired
    public LoginController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest body) {
        log.info("IN login: phone-number: {}, password: {}", body.getPhoneNumber(), body.getPassword());

        try {
            var result = userService.verifyUser(body.getPhoneNumber(), body.getPassword());
            var response = TokenResponse.builder()
                    .accessToken(result.get("accessToken"))
                    .accessTokenExpiration(result.get("accessTokenExpiration"))
                    .refreshToken(result.get("refreshToken"))
                    .refreshTokenExpiration(result.get("refreshTokenExpiration"))
                    .build();

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED).body(
                    TokenResponse.builder()
                            .error(e.getMessage())
                            .build()
            );
        }
    }
}