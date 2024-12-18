package com.daniellaera.backend.utils;

import java.util.Random;

public class NameGenerator {

    private static final String[] ADJECTIVES = {
            "Fancy", "Cool", "Creative", "Brave", "Witty", "Sharp", "Bold", "Swift", "Bright", "Daring"
    };
    private static final String[] NOUNS = {
            "Lion", "Falcon", "Eagle", "Tiger", "Phoenix", "Wolf", "Dragon", "Panther", "Hawk", "Shark"
    };

    private static final Random RANDOM = new Random();

    public static String generateRandomName() {
        String adjective = ADJECTIVES[RANDOM.nextInt(ADJECTIVES.length)];
        String noun = NOUNS[RANDOM.nextInt(NOUNS.length)];
        return adjective + noun;
    }

    public static String generateUniqueSuffix() {
        return Long.toHexString(System.currentTimeMillis()).substring(6); // Short unique suffix
    }
}