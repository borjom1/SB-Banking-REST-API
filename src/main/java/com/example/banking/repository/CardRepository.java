package com.example.banking.repository;

import com.example.banking.entity.CardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<CardEntity, Integer> {
    Optional<CardEntity> findByCardNumber(String cardNumber);
}