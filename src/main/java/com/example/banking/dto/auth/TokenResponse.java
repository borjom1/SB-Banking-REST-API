package com.example.banking.dto.auth;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class TokenResponse {
    private String refreshToken;
    private String accessToken;
    private String error;
}