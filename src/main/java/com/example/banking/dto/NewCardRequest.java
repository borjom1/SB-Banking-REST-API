package com.example.banking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class NewCardRequest {

    @NotNull
    @NotBlank
    private String provider;

    @NotNull
    @NotBlank
    private String currency;

    @NotNull
    @NotBlank
    private String type;
}