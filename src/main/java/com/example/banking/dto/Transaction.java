package com.example.banking.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Transaction {

    private Date performedAt;

    private String partnerName;

    private String partnerCardNumber;

    private BigDecimal sum;

    private BigDecimal commission;

    private String currency;

    private boolean isPartnerSender;

}