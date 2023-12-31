package com.example.banking.dto.card;

import lombok.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Transaction {

    private ZonedDateTime performedAt;

    private String partnerName;

    private String partnerCardNumber;

    private BigDecimal sum;

    private BigDecimal commission;

    private String currency;

    private boolean isPartnerSender;

}