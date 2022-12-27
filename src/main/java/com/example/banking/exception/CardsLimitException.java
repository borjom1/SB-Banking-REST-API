package com.example.banking.exception;

public class CardsLimitException extends Exception {
    public CardsLimitException(String message) {
        super(message);
    }
}
