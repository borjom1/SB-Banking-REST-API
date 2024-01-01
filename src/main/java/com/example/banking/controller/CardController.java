package com.example.banking.controller;

import com.example.banking.dto.card.Card;
import com.example.banking.dto.card.NewCardRequest;
import com.example.banking.dto.card.Transaction;
import com.example.banking.dto.card.TransactionRequest;
import com.example.banking.security.jwt.JWTUserDetails;
import com.example.banking.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;

@Tag(
        name = "Card management",
        description = "End-points for managing own funds"
)
@Slf4j
@RestController
@RequestMapping("/user/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @Operation(summary = "Retrieve all user's cards")
    @GetMapping
    public List<Card> getAllCards(@NonNull Authentication auth) {
        var principal = (JWTUserDetails) auth.getPrincipal();
        log.debug("-> getAllCards(): for={}", principal.getPhoneNumber());

        return cardService.getAllCards(principal.getId());
    }

    @Operation(summary = "Retrieve cvv for specific card by its id")
    @GetMapping("/{id}")
    public String getCvv(@PathVariable long id, @NonNull Authentication auth) {
        log.debug("-> getCvv(): card-id={}", id);
        var principal = (JWTUserDetails) auth.getPrincipal();

        return cardService.getCvv(principal.getId(), id);
    }

    @Operation(summary = "Create a new card")
    @ResponseStatus(CREATED)
    @PostMapping
    public void createCard(@Valid @RequestBody NewCardRequest request,
                           @NonNull Authentication auth) {

        var principal = (JWTUserDetails) auth.getPrincipal();
        log.debug("-> createCard(): user-id={}, opts={}", principal.getId(), request);

        cardService.createCard(request, principal.getId());
    }

    @Operation(summary = "Retrieve all performed transfers for specified card")
    @GetMapping("/{id}/transfers")
    public List<Transaction> getTransactions(@PathVariable long id, @NonNull Authentication auth) {
        var principal = (JWTUserDetails) auth.getPrincipal();
        log.debug("-> getTransactions(): user-id={}, card-id={}", principal.getId(), id);

        return cardService.getAllTransactions(principal.getId(), id);
    }

    @Operation(summary = "Perform transaction between two cards")
    @ResponseStatus(CREATED)
    @PostMapping("/transfer")
    public void createTransaction(@Valid @RequestBody TransactionRequest request,
                                  @NonNull Authentication auth) {

        var principal = (JWTUserDetails) auth.getPrincipal();
        log.debug("-> createTransaction(): user-id={}", principal.getId());

        cardService.performTransaction(principal.getId(), request);
    }

}
