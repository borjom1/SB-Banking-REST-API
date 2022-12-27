package com.example.banking.repository;

import com.example.banking.entity.CurrencyTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CurrencyRepository extends JpaRepository<CurrencyTypeEntity, Integer> {
    Optional<CurrencyTypeEntity> findByName(String currencyName);
}