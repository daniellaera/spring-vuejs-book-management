package com.daniellaera.backend.utils;

import java.util.Random;

public class NameGenerator {

    private static final String[] ADJECTIVES = {
            "Agile", "Ancient", "Bold", "Brilliant", "Calm", "Clever", "Courageous",
            "Daring", "Dynamic", "Elegant", "Epic", "Fierce", "Gallant", "Glorious",
            "Graceful", "Heroic", "Infinite", "Invincible", "Loyal", "Majestic",
            "Mighty", "Noble", "Radiant", "Resilient", "Sharp", "Silent", "Swift",
            "Tenacious", "Valiant", "Vibrant", "Wise", "Witty", "Zealous", "Ethereal",
            "Fearless", "Mystic", "Playful", "Proud", "Serene", "Spirited", "Vivid"
    };

    private static final String[] NOUNS = {
            "Albatross", "Bear", "Cheetah", "Dragon", "Eagle", "Falcon", "Fox",
            "Griffin", "Hawk", "Jaguar", "Leopard", "Lion", "Lynx", "Panther",
            "Phoenix", "Raven", "Shark", "Tiger", "Viper", "Wolf", "Zebra",
            "Cobra", "Bison", "Crane", "Stallion", "Pegasus", "Orca", "Husky",
            "Owl", "Puma", "Serpent", "Otter", "Dolphin", "Penguin", "Ram",
            "Mustang", "Tortoise", "Crocodile", "Koala", "Panda", "Elephant",
            "Hedgehog", "Antelope", "Foxhound", "Sphinx", "Unicorn", "Mongoose"
    };

    private static final Random RANDOM = new Random();

    public static String generateRandomName() {
        String adjective = ADJECTIVES[RANDOM.nextInt(ADJECTIVES.length)];
        String noun = NOUNS[RANDOM.nextInt(NOUNS.length)];
        return adjective + " " + noun;
    }

    public static String generateUniqueSuffix() {
        return Long.toHexString(System.currentTimeMillis()) + Integer.toHexString(new Random().nextInt(1000));
    }
}