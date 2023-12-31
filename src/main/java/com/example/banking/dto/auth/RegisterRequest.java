package com.example.banking.dto.auth;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@ToString
@Builder
public class RegisterRequest {

    @NotNull(message = "not present")
    @Size(min = 3, max = 20, message = "length must be in range from 3 to 20")
    private String firstName;

    @NotNull(message = "not present")
    @Size(min = 3, max = 20, message = "length must be in range from 3 to 20")
    private String lastName;

    @NotNull(message = "not present")
    @Size(min = 13, max = 13, message = "length must be 13")
    private String phoneNumber;

    @NotNull(message = "not present")
    @Size(min = 10, max = 10, message = "length must be 10")
    private String ipn;

    @NotNull(message = "not present")
    @Size(min = 3, max = 20, message = "length must be in range from 3 to 20")
    private String password;

}