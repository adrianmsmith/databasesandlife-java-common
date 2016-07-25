package com.databasesandlife.util;

import java.util.Iterator;

/**
 * Wraps an Iterator, such that as soon as the iterator's hasNext method returns false, user-supplied code is executed.
 * This can be used for closing JDBC Connections, Statements, etc.
 * 
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 */
public class ResourceClosingIterator<T> implements Iterator<T> {
    
    Iterator<T> source;
    Runnable onClose;
    boolean closingDone = false;
    
    /** @param onClose to be run when the iterator's hasNext method returns false
      * @param source the iterator to supply the data which will be returned from this iterator */
    public ResourceClosingIterator(Runnable onClose, Iterator<T> source) {
        this.source = source;
        this.onClose = onClose;
    }

    public synchronized boolean hasNext() {
        boolean result = source.hasNext();
        if (result == false && closingDone == false) {
            onClose.run();
            closingDone = true;
        }
        return result;
    }

    public T next() {
        return source.next();
    }

    public void remove() {
        source.remove();
    }
}
