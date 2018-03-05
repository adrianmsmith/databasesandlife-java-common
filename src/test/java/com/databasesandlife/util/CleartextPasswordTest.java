package com.databasesandlife.util;

import junit.framework.TestCase;

public class CleartextPasswordTest extends TestCase {

    protected void assertContains(String character) {
        for (int i = 0; i < 100; i++)
            if (CleartextPassword.newRandom(100).getCleartext().contains(character)) return;
        fail("Character="+character);
    }

    public void testNewRandom() throws Exception {
        assertEquals(100, CleartextPassword.newRandom(100).getCleartext().length());
        for (int i = 0; i < 100; i++) assertFalse(CleartextPassword.newRandom(100).getCleartext().contains("0"));
        assertContains("A");
        assertContains("a");
        assertContains("8");
    }
}