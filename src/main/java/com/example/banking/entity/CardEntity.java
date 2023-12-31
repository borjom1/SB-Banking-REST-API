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
    private Long id;

    private String cardNumber;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "owner_id")
    private UserEntity owner;

    private ZonedDateTime createdAt;
    private String cvvCode;
    private LocalDate expiryDate;
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
    private Integer sumLimit;

    @Column(name = "blocked")
    private boolean isBlocked;

}