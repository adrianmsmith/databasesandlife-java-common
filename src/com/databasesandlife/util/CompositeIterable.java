package com.databasesandlife.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Wraps a number of iterables into a single iterable.
 * This is especially useful when the iterables themselves are futures.
 * 
 * @param <T> The objects to be iterated over
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @version $Revision$
 */
public class CompositeIterable<T> implements Iterable<T> {
    
    Iterable<? extends Iterable<? extends T>> iterables;
    
    class I implements Iterator<T> {
        Iterator<? extends T> currentIteratorWithinIterable; // null means finished
        Iterator<? extends Iterable<? extends T>> iteratorThroughIterables;
        
        public I() {
            iteratorThroughIterables = iterables.iterator();
            if (iteratorThroughIterables.hasNext()) currentIteratorWithinIterable = iteratorThroughIterables.next().iterator();
            else currentIteratorWithinIterable = null;
        }
        
        @Override public boolean hasNext() {
            while (currentIteratorWithinIterable != null && ! currentIteratorWithinIterable.hasNext()) {
                if (iteratorThroughIterables.hasNext()) currentIteratorWithinIterable = iteratorThroughIterables.next().iterator();
                else currentIteratorWithinIterable = null;
            }
            return currentIteratorWithinIterable != null;
        }
        
        @Override public T next() {
            if ( ! hasNext()) throw new NoSuchElementException();
            return currentIteratorWithinIterable.next();
        }

        @Override public void remove() { throw new UnsupportedOperationException(); }
    }
    
    public CompositeIterable(Iterable<? extends Iterable<? extends T>> iterables) {
        this.iterables = iterables;
    }

    @Override public Iterator<T> iterator() {
        return new I();
    }
}
