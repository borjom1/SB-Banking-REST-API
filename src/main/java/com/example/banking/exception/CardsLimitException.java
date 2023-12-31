package com.example.banking.exception;

public class CardsLimitException extends RuntimeException {
    public CardsLimitException(String message) {
        super(message);
    }
}
