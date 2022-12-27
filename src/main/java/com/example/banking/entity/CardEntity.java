package com.example.banking.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;

@Entity
@Table(name = "cards")
@Getter
@Setter
@ToString(exclude = "owner")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "card_number")
    private String cardNumber;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "owner_id")
    private UserEntity owner;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @Column(name = "cvv_code")
    private String cvvCode;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "pin_code")
    private String pinCode;

    @ManyToOne
    @JoinColumn(name = "type_id")
    private CardTypeEntity cardType;

    @ManyToOne
    @JoinColumn(name = "currency_id")
    private CurrencyTypeEntity currencyType;

    @ManyToOne
    @JoinColumn(name = "provider_id")
    private CardProviderEntity provider;

    private BigDecimal sum;

    @Column(name = "sum_limit")
    private Integer sumLimit;

    @Column(name = "blocked")
    private boolean isBlocked;
}