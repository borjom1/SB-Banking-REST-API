package com.example.banking.service.impl;

import com.example.banking.dto.Card;
import com.example.banking.entity.CardEntity;
import com.example.banking.entity.UserEntity;
import com.example.banking.exception.CardNotFoundException;
import com.example.banking.repository.UserRepository;
import com.example.banking.service.CardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
public class CardServiceImpl implements CardService {

    private final UserRepository userRepository;

    @Autowired
    public CardServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<Card> getAllCards(Integer userId) {
        log.info("IN CardService -> getAllCards()");

        // user will be present if he pass JWTFilter
        UserEntity user = userRepository.findById(userId).get();

        List<CardEntity> cards = new java.util.ArrayList<>(user.getCards().stream().toList());
        cards.sort(Comparator.comparing(CardEntity::getCreatedAt).reversed());

        return cards.stream().map(cardEntity -> {
            LocalDate expiryDate = cardEntity.getExpiryDate();
            return new Card(
                    cardEntity.getId(),
                    cardEntity.getCardType().getName(),
                    cardEntity.getCurrencyType().getName(),
                    cardEntity.getProvider().getName(),
                    cardEntity.getSum(),
                    cardEntity.getCardNumber(),
                    String.format("%d/%d", expiryDate.getMonth().getValue(), expiryDate.getYear() % 100)
            );
        }).toList();
    }

    @Override
    public String getCvv(Integer userId, Integer cardId) throws CardNotFoundException {
        log.info("IN CardService -> getCvv(): card-id:{}", cardId);

        // user will be present if he pass JWTFilter
        UserEntity user = userRepository.findById(userId).get();

        CardEntity card = user.getCards().stream()
                .filter(cardEntity -> cardEntity.getId().equals(cardId))
                .findAny()
                .orElseThrow(() -> new CardNotFoundException(String.format("Card with id:%d not exist", cardId)));

        log.info("IN CardService -> getCvv(): card-id:{} - success", cardId);
        return card.getCvvCode();
    }
}
