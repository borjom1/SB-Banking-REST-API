package com.example.banking.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class RefreshRequest {

    @NotNull(message = "not present")
    @NotBlank(message = "is blank")
    private String refreshToken;

}