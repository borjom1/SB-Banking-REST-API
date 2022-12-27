package com.example.banking.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
@ToString
public class TransactionRequest {

    @NotNull
    private Integer senderCardId;

    @NotNull
    @Size(min = 16, max = 16)
    private String receiverCardNumber;

    @NotNull
    private BigDecimal sum;

    @NotNull
    private String purpose;

}