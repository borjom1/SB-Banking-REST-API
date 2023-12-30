package com.example.banking.dto;

import java.time.ZonedDateTime;

public record ApiError(
        String uri,
        ZonedDateTime timestamp,
        int statusCode,
        String error) {
}
