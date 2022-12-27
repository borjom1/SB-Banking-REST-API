package com.example.banking.repository;

import com.example.banking.entity.CardTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CardTypeRepository extends JpaRepository<CardTypeEntity, Integer> {
    Optional<CardTypeEntity> findByName(String typeName);
}