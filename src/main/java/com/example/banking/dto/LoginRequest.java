package com.example.banking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class LoginRequest {

    @NotNull
    @NotBlank
    @Size(min = 13, max = 13)
    private String phoneNumber;

    @NotNull
    @NotBlank
    @Size(min = 3, max = 20)
    private String password;

}