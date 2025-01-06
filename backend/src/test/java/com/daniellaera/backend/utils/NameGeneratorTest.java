package com.daniellaera.backend.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class NameGeneratorTest {

    @Test
    void testGenerateRandomName() {
        // Generate a random name
        String randomName = NameGenerator.generateRandomName();

        // Ensure the generated name is not null or empty
        assertNotNull(randomName, "Generated name should not be null");
        assertFalse(randomName.isEmpty(), "Generated name should not be empty");

        // Ensure the name contains only valid components
        String[] components = randomName.split("(?=[A-Z])");
        assertEquals(2, components.length, "Generated name should contain an adjective and a noun");
    }

    @Test
    void testGenerateUniqueSuffix() {
        // Generate two unique suffixes
        String suffix1 = NameGenerator.generateUniqueSuffix();
        String suffix2 = NameGenerator.generateUniqueSuffix();

        // Ensure suffixes are not null or empty
        assertNotNull(suffix1, "Generated suffix should not be null");
        assertFalse(suffix1.isEmpty(), "Generated suffix should not be empty");
        assertNotNull(suffix2, "Generated suffix should not be null");
        assertFalse(suffix2.isEmpty(), "Generated suffix should not be empty");

        // Ensure suffixes are unique
        assertNotEquals(suffix1, suffix2, "Generated suffixes should be unique");
    }
}
