package com.example.banking.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Set;

@Entity
@Table(name = "currency_types")
@Getter
public class CurrencyTypeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name = "buying_exchange_rate")
    private BigDecimal buyingExchangeRate;

    @Column(name = "sales_exchange_rate")
    private BigDecimal salesExchangeRate;

    @Column(name = "commission")
    private double commission;

    @JsonIgnore
    @OneToMany(mappedBy = "currencyType")
    private Set<CardEntity> cards;

}
