package com.example.banking.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Builder
public class LoginRequest {

    @NotNull
    @Size(min = 13, max = 13)
    private String phoneNumber;

    @NotNull
    @Size(min = 3, max = 20)
    private String password;

}