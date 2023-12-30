package com.example.banking.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@ToString
@Builder
public class RegisterRequest {

    @NotNull
    @Size(max = 20)
    private String firstName;

    @NotNull
    @Size(max = 20)
    private String lastName;

    @NotNull
    @Size(min = 13, max = 13)
    private String phoneNumber;

    @NotNull
    @Size(min = 10, max = 10)
    private String ipn;

    @NotNull
    @Size(min = 3, max = 20)
    private String password;

}