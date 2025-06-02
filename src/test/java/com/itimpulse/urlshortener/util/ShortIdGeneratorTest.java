package com.itimpulse.urlshortener.util;

import org.junit.jupiter.api.RepeatedTest;

import static org.junit.jupiter.api.Assertions.*;

class ShortIdGeneratorTest {

    private final ShortIdGenerator generator = new ShortIdGenerator();

    @RepeatedTest(10)
    void testGenerateProducesValidShortId() {
        String id = generator.generate();
        assertNotNull(id);
        assertEquals(6, id.length());
        assertTrue(id.matches("^[a-zA-Z0-9]{6}$"));
    }

    @RepeatedTest(10)
    void testGenerateAlwaysDifferent() {
        String id1 = generator.generate();
        String id2 = generator.generate();
        assertNotEquals(id1, id2);
    }
}