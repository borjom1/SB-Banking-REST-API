package com.example.banking.service;

import com.example.banking.dto.Card;
import com.example.banking.dto.NewCardRequest;
import com.example.banking.dto.Transaction;
import com.example.banking.exception.CardNotFoundException;
import com.example.banking.exception.CardsLimitException;
import com.example.banking.exception.ViolationPrivacyException;

import java.util.List;

public interface CardService {
    void createCard(NewCardRequest request, Integer userId) throws CardsLimitException;

    List<Card> getAllCards(Integer userId);

    String getCvv(Integer userId, Integer cardId) throws CardNotFoundException;

    List<Transaction> getAllTransactions(Integer userId, Integer cardId) throws ViolationPrivacyException;
}
