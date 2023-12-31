package com.example.banking.dto.card;

import com.example.banking.dto.card.CardType;
import com.example.banking.dto.card.Currency;
import com.example.banking.dto.card.Provider;
import com.example.banking.dto.validator.EnumValue;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class NewCardRequest {

    @NotNull(message = "not present")
    @EnumValue(enumClass = Provider.class)
    private String provider;

    @NotNull(message = "not present")
    @EnumValue(enumClass = Currency.class)
    private String currency;

    @NotNull(message = "not present")
    @EnumValue(enumClass = CardType.class)
    private String cardType;

}