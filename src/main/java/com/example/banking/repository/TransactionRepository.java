package com.example.banking.repository;

import com.example.banking.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {
    @Query("FROM TransactionEntity WHERE sender.id = :cardId OR receiver.id = :cardId ORDER BY time DESC")
    List<TransactionEntity> getAllByCardId(Long cardId);
}