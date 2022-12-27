package com.example.banking.util;

import java.util.Random;

public class NumberGenerator {
    public static final int ACCOUNT_LENGTH = 12;
    public static final int PIN_LENGTH = 4;
    public static final int CVV_LENGTH = 3;
    private static final String SOURCE = "0123456789";
    private static final StringBuilder builder = new StringBuilder();
    private static final Random random = new Random();

    public static String generate(int length) {
        builder.setLength(0);
        for (int i = 0; i < length; i++) {
            builder.append(SOURCE.charAt(random.nextInt(0, SOURCE.length())));
        }
        return builder.toString();
    }
}