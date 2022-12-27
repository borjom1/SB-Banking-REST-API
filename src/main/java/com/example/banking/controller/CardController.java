package com.example.banking.controller;

import com.example.banking.dto.Card;
import com.example.banking.dto.NewCardRequest;
import com.example.banking.exception.CardNotFoundException;
import com.example.banking.exception.CardsLimitException;
import com.example.banking.security.jwt.JWTUserDetails;
import com.example.banking.service.CardService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

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
}
