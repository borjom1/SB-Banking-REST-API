package com.example.banking.dto.card;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@AllArgsConstructor
@Getter
public class Card {
    private Long id;
    private String type;
    private String currency;
    private String provider;
    private BigDecimal sum;
    private String cardNumber;
    private String expireDate; // month/year format
    private boolean isBlocked;
}