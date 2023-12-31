package com.example.banking.dto.auth;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Builder
public class LoginRequest {

    @NotNull(message = "not present")
    @Size(min = 13, max = 13, message = "length must be 13")
    private String phoneNumber;

    @NotNull(message = "not present")
    @Size(min = 3, max = 20, message = "length must be in range from 3 to 20")
    private String password;

}