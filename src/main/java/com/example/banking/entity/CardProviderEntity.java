package com.example.banking.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.Set;

@Entity
public class CardProviderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    private String code;

    @JsonIgnore
    @OneToMany(mappedBy = "provider")
    private Set<CardEntity> cards;

}
