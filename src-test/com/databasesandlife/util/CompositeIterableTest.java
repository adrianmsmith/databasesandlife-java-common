package com.databasesandlife.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

import junit.framework.TestCase;

/**
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @see <a href="https://github.com/adrianmsmith/databasesandlife-java-common">Project on GitHub</a>
 */
public class CompositeIterableTest extends TestCase {
    
    public void testHasNext() {
        Iterator<String> i = new CompositeIterable<String>(Arrays.asList(
            new ArrayList<String>(),
            Arrays.asList("a", "b"), 
            new ArrayList<String>(),
            Arrays.asList("c"),
            new ArrayList<String>()
        )).iterator();
        assertTrue(i.hasNext());
        i.next(); // a
        assertTrue(i.hasNext());
        i.next(); // b
        assertTrue(i.hasNext());
        i.next(); // c
        assertFalse(i.hasNext());
    }

    public void testNext() {
        Iterator<String> i = new CompositeIterable<String>(Arrays.asList(
                new ArrayList<String>(),
                Arrays.asList("a", "b"), 
                new ArrayList<String>(),
                Arrays.asList("c"),
                new ArrayList<String>()
            )).iterator();
        assertEquals("a", i.next());
        assertEquals("b", i.next());
        assertEquals("c", i.next());
        try { i.next(); fail(); } catch (NoSuchElementException e) { }
    }
}
