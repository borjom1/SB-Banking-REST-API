package com.example.banking.service.impl;

import com.example.banking.dto.Card;
import com.example.banking.dto.NewCardRequest;
import com.example.banking.entity.*;
import com.example.banking.exception.CardNotFoundException;
import com.example.banking.exception.CardsLimitException;
import com.example.banking.repository.*;
import com.example.banking.service.CardService;
import com.example.banking.util.NumberGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
public class CardServiceImpl implements CardService {

    private final static int CARDS_LIMIT = 5;
    private final static int DEFAULT_SUM_LIMIT = 500;

    private final UserRepository userRepository;
    private final CardRepository cardRepository;
    private final CardProviderRepository cardProviderRepository;
    private final CurrencyRepository currencyRepository;
    private final CardTypeRepository cardTypeRepository;

    @Autowired
    public CardServiceImpl(UserRepository userRepository, CardRepository cardRepository, CardProviderRepository cardProviderRepository,
                           CurrencyRepository currencyRepository, CardTypeRepository cardTypeRepository) {
        this.userRepository = userRepository;
        this.cardRepository = cardRepository;
        this.cardProviderRepository = cardProviderRepository;
        this.currencyRepository = currencyRepository;
        this.cardTypeRepository = cardTypeRepository;
    }

    @Override
    public void createCard(NewCardRequest request, Integer userId) throws CardsLimitException {
        log.info("IN CardService -> createCard() user-id:{}", userId);

        // user will be present if he pass JWTFilter
        UserEntity user = userRepository.findById(userId).get();
        if (user.getCards().size() == CARDS_LIMIT) {
            throw new CardsLimitException(String.format("User can not have more than %d cards", CARDS_LIMIT));
        }

        // validate request options
        CardProviderEntity selectedProvider = cardProviderRepository.findByName(request.getProvider())
                .orElseThrow(() -> new BadCredentialsException("Card provider not found"));

        CurrencyTypeEntity selectedCurrency = currencyRepository.findByName(request.getCurrency())
                .orElseThrow(() -> new BadCredentialsException("Currency not found"));

        CardTypeEntity selectedCardType = cardTypeRepository.findByName(request.getType())
                .orElseThrow(() -> new BadCredentialsException("Type of card not found"));


        String generatedCardNumber;
        do {
            generatedCardNumber = selectedProvider.getCode() + NumberGenerator.generate(NumberGenerator.ACCOUNT_LENGTH);
        } while (cardRepository.findByCardNumber(generatedCardNumber).isPresent());

        // build card
        CardEntity newCard = CardEntity.builder()
                .cardNumber(generatedCardNumber)
                .createdAt(ZonedDateTime.now())
                .provider(selectedProvider)
                .cardType(selectedCardType)
                .currencyType(selectedCurrency)
                .expiryDate(LocalDate.of(LocalDate.now().getYear() + 2, Month.DECEMBER, 1))
                .cvvCode(NumberGenerator.generate(NumberGenerator.CVV_LENGTH))
                .pinCode(NumberGenerator.generate(NumberGenerator.PIN_LENGTH))
                .sum(BigDecimal.ZERO)
                .sumLimit(DEFAULT_SUM_LIMIT)
                .isBlocked(false)
                .owner(user)
                .build();

        // save card
        user.getCards().add(newCard);
        cardRepository.save(newCard);
        log.info("IN CardService -> createCard(): created card:{} for user:{}", newCard.getId(), userId);
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
