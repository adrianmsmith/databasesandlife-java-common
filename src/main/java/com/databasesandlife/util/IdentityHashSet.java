package com.databasesandlife.util;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Iterator;

/**
 * Like a {@link java.util.HashSet} but compares objects based on the Java object identity.
 *
 * <p>Does not implement {@link java.util.Set} interface because it is less generic than it could be;
 * and in the project where this was required implementing the interface was not required.</p>
 *
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @see <a href="https://github.com/adrianmsmith/databasesandlife-java-common">Project on GitHub</a>
 */
class IdentityHashSet<T> {

    protected final IdentityHashMap<T, Object> map = new IdentityHashMap<>();

    // Constructors
    public IdentityHashSet() { }
    public IdentityHashSet(Collection<T> collection) { for (T x : collection) add(x); }

    // Mutators
    public void add(T obj) { map.put(obj, null); }
    public void remove(T obj) { map.remove(obj); }

    // Query
    public boolean contains(T obj) { return map.containsKey(obj); }
    public Iterator<T> iterator() { return map.keySet().iterator(); }
    public boolean isEmpty() { return map.isEmpty(); }
}
