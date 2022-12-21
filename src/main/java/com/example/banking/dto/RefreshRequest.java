package com.example.banking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class RefreshRequest {
    @NotNull
    @NotBlank
    private String refreshToken;
}