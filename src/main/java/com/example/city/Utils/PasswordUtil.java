package com.example.city.Utils;

import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;


@Component
public class PasswordUtil {
    private static final String chars =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";

    public String generatePassword(int length) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < length; i++) {
            int index = ThreadLocalRandom.current().nextInt(chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }
}
