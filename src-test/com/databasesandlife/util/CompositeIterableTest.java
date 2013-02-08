package com.databasesandlife.util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

import junit.framework.TestCase;

public class CompositeIterableTest extends TestCase {
    
    public void testHasNext() {
        Iterator<String> i = new CompositeIterable<String>(Arrays.asList(Arrays.asList("a", "b"), Arrays.asList("c"))).iterator();
        assertTrue(i.hasNext());
        i.next(); // a
        assertTrue(i.hasNext());
        i.next(); // b
        assertTrue(i.hasNext());
        i.next(); // c
        assertFalse(i.hasNext());
    }

    public void testNext() {
        Iterator<String> i = new CompositeIterable<String>(Arrays.asList(Arrays.asList("a", "b"), Arrays.asList("c"))).iterator();
        assertEquals("a", i.next());
        assertEquals("b", i.next());
        assertEquals("c", i.next());
        try { i.next(); fail(); } catch (NoSuchElementException e) { }
    }
}
