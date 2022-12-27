package com.example.banking.controller;

import com.example.banking.dto.Card;
import com.example.banking.dto.NewCardRequest;
import com.example.banking.dto.Transaction;
import com.example.banking.dto.TransactionRequest;
import com.example.banking.exception.CardCredentialsException;
import com.example.banking.exception.CardNotFoundException;
import com.example.banking.exception.CardsLimitException;
import com.example.banking.exception.ViolationPrivacyException;
import com.example.banking.security.jwt.JWTUserDetails;
import com.example.banking.service.CardService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/user/card")
public class CardController {

    private final CardService cardService;

    @Autowired
    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @GetMapping("/all")
    public List<Card> getAllCards() {
        var principal = (JWTUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("IN CardController -> getAllCards(): phone-number: {} user", principal.getPhoneNumber());
        return cardService.getAllCards(principal.getId());
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getCvv(@PathVariable int id) {
        try {
            log.info("IN CardController -> getCvv(): card-id:{}", id);
            var principal = (JWTUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            return ResponseEntity.ok(cardService.getCvv(principal.getId(), id));
        } catch (CardNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/new")
    public ResponseEntity<Map<String, String>> createCard(@Valid @RequestBody NewCardRequest request) {
        Map<String, String> result = new HashMap<>();
        try {
            var principal = (JWTUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            log.info("IN CardController -> createCard() for user-id:{}, opts = {}", principal.getId(), request);
            cardService.createCard(request, principal.getId());
            result.put("message", "success");
            return ResponseEntity.ok(result);
        } catch (BadCredentialsException | CardsLimitException e) {
            result.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @GetMapping("/{id}/transfers")
    public ResponseEntity<List<Transaction>> getTransactions(@PathVariable("id") int cardId) {
        var principal = (JWTUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        try {
            log.info("IN CardController -> getTransactions() for user-id:{}, card-id: {}", principal.getId(), cardId);
            var transactions = cardService.getAllTransactions(principal.getId(), cardId);
            return ResponseEntity.ok(transactions);
        } catch (BadCredentialsException | ViolationPrivacyException e) {
            log.warn("IN CardController -> getTransactions() for user-id:{}, exception: {}", principal.getId(), e.getMessage());
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }
    }

    @PostMapping("/transfer")
    public ResponseEntity<Map<String, String>> createTransaction(@Valid @RequestBody TransactionRequest request) {
        Map<String, String> result = new HashMap<>();
        var principal = (JWTUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        try {
            log.info("IN CardController -> createTransaction() for user-id:{}", principal.getId());
            cardService.performTransaction(principal.getId(), request);
            result.put("message", "success");
            return ResponseEntity.ok(result);
        } catch (ViolationPrivacyException | CardNotFoundException | CardCredentialsException e) {
            result.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }
    
}
