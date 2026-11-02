package com.fintech.leanbackend.utils;

import java.security.SecureRandom;
import java.util.UUID;

public class KeyGenerator {

    private static final SecureRandom random = new SecureRandom();
    private static final String UPPERCASE_LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static String generateCustomKey() {
        StringBuilder key = new StringBuilder();
        key.append("Jk-");

        int num1 = random.nextInt(10_000_000);
        key.append(String.format("%07d", num1)); // %07d pads with leading zeros

        key.append(getRandomLetters(2));

        int num2 = random.nextInt(1000);
        key.append(String.format("%03d", num2)); // %03d pads with leading zeros

        return key.toString();
    }


    private static String getRandomLetters(int length) {
        StringBuilder letters = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(UPPERCASE_LETTERS.length());
            letters.append(UPPERCASE_LETTERS.charAt(randomIndex));
        }
        return letters.toString();
    }


    public static String getOtpKeys(){
        return String.valueOf(100000 + random.nextInt(900000));
    }

}