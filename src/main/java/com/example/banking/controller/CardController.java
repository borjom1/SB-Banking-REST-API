package com.example.banking.controller;

import com.example.banking.dto.Card;
import com.example.banking.security.jwt.JWTUserDetails;
import com.example.banking.service.CardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
}
