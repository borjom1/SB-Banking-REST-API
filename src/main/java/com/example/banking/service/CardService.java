package com.example.banking.service;

import com.example.banking.dto.Card;
import com.example.banking.exception.CardNotFoundException;

import java.util.List;

public interface CardService {
    List<Card> getAllCards(Integer userId);

    String getCvv(Integer userId, Integer cardId) throws CardNotFoundException;
}
