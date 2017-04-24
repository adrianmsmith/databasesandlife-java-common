package com.databasesandlife.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Takes an array of iterators, and returns their results one after another.
 * 
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @see <a href="https://github.com/adrianmsmith/databasesandlife-java-common">Project on GitHub</a>
 */
public class CompositeIterator<T> implements Iterator<T> {
    
    Iterator<T>[] children;
    int nextElementFrom = 0; // set to children.length means finished
    
    public CompositeIterator(Iterator<T>[] children) {
        this.children = children;
        advanceIfNecessary();
    }

    protected void advanceIfNecessary() {
        while (nextElementFrom < children.length && !children[nextElementFrom].hasNext()) nextElementFrom++;
    }

    public boolean hasNext() {
        return nextElementFrom < children.length;
    }
    
    public T next() {
        if ( ! hasNext()) throw new NoSuchElementException();
        T result = children[nextElementFrom].next();
        advanceIfNecessary();
        return result;
    }

    public void remove() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
