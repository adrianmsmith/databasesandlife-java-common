package com.databasesandlife.util;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class DomNodeListIterable implements Iterable<Node> {

    protected @Nonnull NodeList nodes;

    protected class NodeIterator implements Iterator<Node> {
        int next = 0;

        @Override
        public boolean hasNext() {
            return next < nodes.getLength();
        }

        @Override
        public Node next() {
            if ( ! hasNext()) throw new NoSuchElementException();

            Node result = nodes.item(next);
            next++;
            return result;
        }
    }
    
    public DomNodeListIterable(@Nonnull NodeList nodes) { this.nodes = nodes; }

    @Override
    public @Nonnull Iterator<Node> iterator() {
        return new NodeIterator();
    }
}
