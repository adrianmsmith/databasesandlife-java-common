package com.databasesandlife.util;

import com.databasesandlife.util.CompositeIterator;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Vector;
import junit.framework.TestCase;

/**
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 */
@SuppressWarnings("unchecked")
public class CompositeIteratorTest extends TestCase {
    
    public CompositeIteratorTest(String testName) {
        super(testName);
    }            
    
    public void test() {
        Iterator<Integer> a = new Vector<Integer>().iterator();
        Iterator<Integer> b = Arrays.asList(1, 2, 3).iterator();
        Iterator<Integer> c = new Vector<Integer>().iterator();
        Iterator<Integer> d = Arrays.asList(4).iterator();
        Iterator<Integer> e = new Vector<Integer>().iterator();
        Iterator<Integer>[] array = (Iterator<Integer>[]) Arrays.asList(a, b, c, d, e).toArray();
        
        Iterator<Integer> comp = new CompositeIterator<Integer>(array);
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
