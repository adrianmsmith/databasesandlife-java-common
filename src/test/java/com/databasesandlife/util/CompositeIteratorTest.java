package com.databasesandlife.util;

import com.databasesandlife.util.CompositeIterator;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.ArrayList;
import junit.framework.TestCase;

/**
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @see <a href="https://github.com/adrianmsmith/databasesandlife-java-common">Project on GitHub</a>
 */
@SuppressWarnings("unchecked")
public class CompositeIteratorTest extends TestCase {
    
    public CompositeIteratorTest(String testName) {
        super(testName);
    }            
    
    public void test() {
        Iterator<Integer> a = new ArrayList<Integer>().iterator();
        Iterator<Integer> b = Arrays.asList(1, 2, 3).iterator();
        Iterator<Integer> c = new ArrayList<Integer>().iterator();
        Iterator<Integer> d = Arrays.asList(4).iterator();
        Iterator<Integer> e = new ArrayList<Integer>().iterator();
        Iterator<Integer>[] array = (Iterator<Integer>[]) Arrays.asList(a, b, c, d, e).toArray();
        
        Iterator<Integer> comp = new CompositeIterator<>(array);
        assertTrue(comp.hasNext());
        assertEquals(1, (int) comp.next());
        assertTrue(comp.hasNext());
        assertEquals(2, (int) comp.next());
        assertTrue(comp.hasNext());
        assertEquals(3, (int) comp.next());
        assertTrue(comp.hasNext());
        assertEquals(4, (int) comp.next());
        assertFalse(comp.hasNext());
        
        try { comp.next(); fail(); }
        catch (NoSuchElementException ex) { }
    }
}
