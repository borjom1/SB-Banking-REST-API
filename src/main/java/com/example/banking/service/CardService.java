package com.example.banking.service;

import com.example.banking.dto.card.Card;
import com.example.banking.dto.card.NewCardRequest;
import com.example.banking.dto.card.Transaction;
import com.example.banking.dto.card.TransactionRequest;

import java.util.List;

public interface CardService {
    void createCard(NewCardRequest request, Long userId);
    List<Card> getAllCards(Long userId);
    String getCvv(Long userId, Long cardId);
    List<Transaction> getAllTransactions(Long userId, Long cardId);
    void performTransaction(Long userId, TransactionRequest request);
}
