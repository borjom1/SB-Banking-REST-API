package com.example.banking.repository;

import com.example.banking.entity.CardProviderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CardProviderRepository extends JpaRepository<CardProviderEntity, Integer> {
    Optional<CardProviderEntity> findByName(String providerName);
}