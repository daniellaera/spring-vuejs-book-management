package com.daniellaera.backend.utils;

import java.util.Random;

public class NameGenerator {

    private static final String[] ADJECTIVES = {
            "Agile", "Ancient", "Bold", "Brilliant", "Calm", "Clever", "Courageous",
            "Daring", "Dynamic", "Elegant", "Epic", "Fierce", "Gallant", "Glorious",
            "Graceful", "Heroic", "Infinite", "Invincible", "Loyal", "Majestic",
            "Mighty", "Noble", "Radiant", "Resilient", "Sharp", "Silent", "Swift",
            "Tenacious", "Valiant", "Vibrant", "Wise", "Witty", "Zealous", "Ethereal",
            "Fearless", "Mystic", "Playful", "Proud", "Serene", "Spirited", "Vivid",
            "Amber", "Arctic", "Astral", "Blazing", "Breezy", "Celestial", "Crimson",
            "Crystal", "Cunning", "Defiant", "Deft", "Devoted", "Distant", "Dreamy",
            "Dusky", "Electric", "Emerald", "Enchanted", "Eternal", "Feral", "Fiery",
            "Frosty", "Gentle", "Gilded", "Grim", "Hallowed", "Humble", "Icy",
            "Jade", "Keen", "Kindred", "Lavish", "Lofty", "Lucid", "Lunar",
            "Marine", "Mellow", "Molten", "Nimble", "Obsidian", "Onyx", "Opulent",
            "Pacific", "Phantom", "Primal", "Pristine", "Quick", "Quiet", "Regal",
            "Roaming", "Rugged", "Rustic", "Sacred", "Savage", "Scarlet", "Shadow",
            "Silver", "Solar", "Somber", "Stark", "Stellar", "Stoic", "Stormy",
            "Stout", "Subtle", "Sunlit", "Tidal", "Timeless", "Twilight", "Untamed",
            "Verdant", "Volcanic", "Wandering", "Wicked", "Wild", "Wintry", "Zen"
    };

    private static final String[] NOUNS = {
            "Albatross", "Bear", "Cheetah", "Dragon", "Eagle", "Falcon", "Fox",
            "Griffin", "Hawk", "Jaguar", "Leopard", "Lion", "Lynx", "Panther",
            "Phoenix", "Raven", "Shark", "Tiger", "Viper", "Wolf", "Zebra",
            "Cobra", "Bison", "Crane", "Stallion", "Pegasus", "Orca", "Husky",
            "Owl", "Puma", "Serpent", "Otter", "Dolphin", "Penguin", "Ram",
            "Mustang", "Tortoise", "Crocodile", "Koala", "Panda", "Elephant",
            "Hedgehog", "Antelope", "Foxhound", "Sphinx", "Unicorn", "Mongoose",
            "Armadillo", "Badger", "Barracuda", "Basilisk", "Bobcat", "Buffalo",
            "Caribou", "Chameleon", "Cicada", "Condor", "Coyote", "Dingo",
            "Dragonfly", "Elk", "Firefly", "Gazelle", "Gecko", "Gorilla",
            "Grizzly", "Heron", "Hornet", "Hyena", "Iguana", "Impala",
            "Jackal", "Jellyfish", "Kestrel", "Kingfisher", "Kraken", "Lemur",
            "Mammoth", "Mantis", "Marlin", "Marten", "Merlin", "Mockingbird",
            "Moose", "Narwhal", "Nighthawk", "Ocelot", "Osprey", "Oyster",
            "Parrot", "Pelican", "Python", "Quail", "Raptor", "Rhino",
            "Salamander", "Scorpion", "Sparrow", "Stingray", "Swallow", "Swordfish",
            "Talon", "Tapir", "Terrier", "Thunderbird", "Toucan", "Trout",
            "Vulture", "Walrus", "Warthog", "Wasp", "Wolverine", "Wren", "Yak"
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