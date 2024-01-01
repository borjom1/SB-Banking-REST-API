package com.example.banking.service.impl;

import com.example.banking.dto.card.Card;
import com.example.banking.dto.card.NewCardRequest;
import com.example.banking.dto.card.Transaction;
import com.example.banking.dto.card.TransactionRequest;
import com.example.banking.entity.*;
import com.example.banking.exception.*;
import com.example.banking.repository.*;
import com.example.banking.service.CardService;
import com.example.banking.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.example.banking.entity.CurrencyTypeEntity.Currency.UAH;
import static com.example.banking.util.NumberGenerator.*;
import static com.example.banking.util.NumberGenerator.generate;
import static java.lang.String.format;
import static java.time.LocalDate.*;
import static java.time.Month.DECEMBER;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    @Value("${banking.cards.precision}")
    private int precision;

    @Value("${banking.cards.limits.count}")
    private int cardsLimit;

    @Value("${banking.cards.limits.txn-sum}")
    private int defaultSumLimit;

    private final UserService userService;

    private final CardRepository cardRepository;
    private final CardProviderRepository cardProviderRepository;
    private final CurrencyRepository currencyRepository;
    private final CardTypeRepository cardTypeRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    @Override
    public void createCard(NewCardRequest request, Long userId) {
        log.info("-> createCard() user-id={}", userId);

        UserEntity user = userService.findUser(userId);
        if (user.getCards().size() == cardsLimit) {
            throw new CardsLimitException("Cards count limit is exceeded");
        }

        // find card provider, currency and card type
        CardProviderEntity selectedProvider = cardProviderRepository.findByName(request.getProvider())
                .orElseThrow(() -> new IllegalStateException("Card provider not found"));

        CurrencyTypeEntity selectedCurrency = currencyRepository.findByName(request.getCurrency())
                .orElseThrow(() -> new IllegalStateException("Currency not found"));

        CardTypeEntity selectedCardType = cardTypeRepository.findByName(request.getCardType())
                .orElseThrow(() -> new IllegalStateException("Type of card not found"));

        String generatedCardNumber;

        // generate card number till it is not unique
        do {
            generatedCardNumber = selectedProvider.getCode() + generate(ACCOUNT_LENGTH);
        } while (cardRepository.findByCardNumber(generatedCardNumber).isPresent());

        // the end of card's validity finishes in two years on 1st December
        LocalDate expiryDate = of(now().getYear() + 2, DECEMBER, 1);

        // build card
        CardEntity newCard = CardEntity.builder()
                .cardNumber(generatedCardNumber)
                .createdAt(ZonedDateTime.now())
                .provider(selectedProvider)
                .cardType(selectedCardType)
                .currencyType(selectedCurrency)
                .expiryDate(expiryDate)
                .cvvCode(generate(CVV_LENGTH))
                .pinCode(generate(PIN_LENGTH))
                .sum(BigDecimal.ZERO)
                .sumLimit(defaultSumLimit)
                .isBlocked(false)
                .owner(user)
                .build();

        // save card
        user.getCards().add(newCard);
        cardRepository.save(newCard);
    }

    @Override
    public List<Card> getAllCards(Long userId) {
        log.debug("-> getAllCards()");

        UserEntity user = userService.findUser(userId);

        List<CardEntity> cards = new ArrayList<>(user.getCards().stream().toList());
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
                    format("%d/%d", expiryDate.getMonth().getValue(), expiryDate.getYear() % 100),
                    cardEntity.isBlocked()
            );
        }).toList();
    }

    @Override
    public String getCvv(Long userId, Long cardId) {
        log.debug("-> getCvv(): card-id={}", cardId);

        UserEntity user = userService.findUser(userId);

        CardEntity card = user.getCards().stream()
                .filter(cardEntity -> cardEntity.getId().equals(cardId))
                .findAny()
                .orElseThrow(() -> new CardNotFoundException("Card does not exist"));

        return card.getCvvCode();
    }

    @Override
    public List<Transaction> getAllTransactions(Long userId, Long cardId) {
        log.debug("-> getAllTransactions(): user-id={} card-id={}", userId, cardId);

        UserEntity user = userService.findUser(userId);

        getCardIfOwner(user, cardId); // this checks if user owns specified card

        // retrieve all transactions performed with specified card by custom native query
        List<TransactionEntity> transactions = transactionRepository.getAllByCardId(cardId);

        return transactions.stream().map(tEntity -> {
            CardEntity senderCard = tEntity.getSender();
            CardEntity receiverCard = tEntity.getReceiver();
            UserEntity partner;

            Transaction.TransactionBuilder tBuilder = Transaction.builder();

            // if user was transfer creator
            if (senderCard.getId().equals(cardId)) {
                partner = receiverCard.getOwner();

                tBuilder.sum(tEntity.getSum())
                        .commission(tEntity.getCommission())
                        .currency(senderCard.getCurrencyType().getName())
                        .partnerCardNumber(receiverCard.getCardNumber())
                        .isPartnerSender(false);
            } else {
                partner = senderCard.getOwner();

                tBuilder.sum(tEntity.getConvertedSum())
                        .commission(tEntity.getConvertedCommission())
                        .currency(receiverCard.getCurrencyType().getName())
                        .partnerCardNumber(senderCard.getCardNumber())
                        .isPartnerSender(true);
            }

            tBuilder.performedAt(tEntity.getTime());
            tBuilder.partnerName(partner.getFirstName() + " " + partner.getLastName());

            return tBuilder.build();
        }).toList();
    }

    @Transactional
    @Override
    public void performTransaction(Long userId, @NonNull TransactionRequest request) {
        log.debug("-> performTransaction(): user-id={} txn={}", userId, request);

        UserEntity sender = userService.findUser(userId);

        // find sender & receiver cards
        CardEntity senderCard = getCardIfOwner(sender, request.getSenderCardId());
        CardEntity receiverCard = cardRepository.findByCardNumber(request.getReceiverCardNumber())
                .orElseThrow(() -> new CardNotFoundException(format("Card[%s] not found", request.getReceiverCardNumber())));

        // define currencies
        var senderCardCurrency = senderCard.getCurrencyType();
        var receiverCardCurrency = receiverCard.getCurrencyType();

        BigDecimal initialSum = request.getSum();
        checkTransactionAvailability(senderCard, initialSum);

        // grab commission
        double sumRate = 1 - senderCardCurrency.getCommission() / 100;
        BigDecimal transactionSum = initialSum.multiply(BigDecimal.valueOf(sumRate));
        BigDecimal commission = initialSum.subtract(transactionSum);

        // conversion
        BigDecimal convertedSum = convertSum(transactionSum, senderCardCurrency, receiverCardCurrency);
        BigDecimal convertedCommission = convertSum(commission, senderCardCurrency, receiverCardCurrency);

//        log.debug("IN CardService -> performTransaction(): sum_rate:{}", sumRate);
//        log.debug("IN CardService -> performTransaction(): initial_sum:{}, without_commission:{}, converted_sum:{}", initialSum, transactionSum, convertedSum);
//        log.debug("IN CardService -> performTransaction(): commission:{}, converted_commission:{}", commission, convertedCommission);

        // make transfer
        senderCard.setSum(senderCard.getSum().subtract(initialSum));
        receiverCard.setSum(receiverCard.getSum().add(convertedSum));

        // build transaction
        TransactionEntity transaction = TransactionEntity.builder()
                .sender(senderCard)
                .receiver(receiverCard)
                .purpose(request.getPurpose())
                .time(ZonedDateTime.now())
                .sum(initialSum)
                .convertedSum(convertedSum)
                .commission(commission)
                .convertedCommission(convertedCommission)
                .build();

        transactionRepository.save(transaction);

        log.debug("-> performTransaction(): user-id={} txn={} - SUCCESS", userId, request);
    }

    private BigDecimal convertSum(@NonNull BigDecimal sum,
                                  @NonNull CurrencyTypeEntity senderCardCurrency,
                                  @NonNull CurrencyTypeEntity receiverCardCurrency) {

        if (senderCardCurrency.getName().equals(UAH.name())) {
            return receiverCardCurrency.getName().equals(UAH.name()) ?
                    sum :
                    sum.divide(receiverCardCurrency.getSalesExchangeRate(), precision, RoundingMode.HALF_UP);
        } else if (receiverCardCurrency.getName().equals(UAH.name())) {
            return sum.multiply(senderCardCurrency.getBuyingExchangeRate());
        } else {
            sum = sum.multiply(senderCardCurrency.getBuyingExchangeRate());
            return sum.divide(receiverCardCurrency.getSalesExchangeRate(), precision, RoundingMode.HALF_UP);
        }
    }

    private void checkTransactionAvailability(@NonNull CardEntity sourceCard, @NonNull BigDecimal txnSum) {

        String errorMsg = null;

        if (sourceCard.isBlocked()) { // check block status
            errorMsg = format("Card[%d] is blocked", sourceCard.getId());

        } else if (sourceCard.getExpiryDate().isBefore(now())) { // check expire date
            errorMsg = format("Card[%d] is expired", sourceCard.getId());

        } else if (sourceCard.getSum().compareTo(txnSum) < 0) { // check sum
            errorMsg = format("Card[%d] not enough funds", sourceCard.getId());

            // check if transaction sum exceeds card's sum limit
        } else if (BigDecimal.valueOf(sourceCard.getSumLimit().longValue()).compareTo(txnSum) < 0) {
            errorMsg = format("Card[%d] limit is exceeded", sourceCard.getId());
        }

        if (errorMsg != null) {
            throw new TransactionNotAvailableException(errorMsg);
        }

    }

    private CardEntity getCardIfOwner(@NonNull UserEntity user, Long cardId) {
        return user.getCards().stream()
                .filter(card -> card.getId().equals(cardId))
                .findAny()
                .orElseThrow(() -> new ViolationPrivacyException("Card not found"));
    }

}
