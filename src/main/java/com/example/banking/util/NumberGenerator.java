package com.example.banking.util;

import java.util.Random;

public class NumberGenerator {
    public static final int ACCOUNT_LENGTH = 12;
    public static final int PIN_LENGTH = 4;
    public static final int CVV_LENGTH = 3;
    private static final String DIGITS = "0123456789";
    private static final Random random = new Random();

    public static String generate(int length) {
        var builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            builder.append(DIGITS.charAt(random.nextInt(0, DIGITS.length())));
        }
        return builder.toString();
    }
}