package com.example.banking.dto.card;

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

    @NotNull(message = "not present")
    private Long senderCardId;

    @NotNull(message = "not present")
    @Size(min = 16, max = 16, message = "length must be 16")
    private String receiverCardNumber;

    @NotNull(message = "not present")
    private BigDecimal sum;

    @NotNull(message = "not present")
    private String purpose;

}