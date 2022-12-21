package com.example.banking.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class TokenResponse {
    private String refreshToken;
    private String refreshTokenExpiration;
    private String accessToken;
    private String accessTokenExpiration;
    private String error;
}