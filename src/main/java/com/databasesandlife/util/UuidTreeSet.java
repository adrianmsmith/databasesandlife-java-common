package com.databasesandlife.util;

import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;
import java.util.UUID;

/**
 * This is an ordered set of UUIDs.
 * Unfortunately a standard TreeSet of UUIDs is broken, it puts them in the wrong order,
 * see <a href="http://bugs.java.com/bugdatabase/view_bug.do?bug_id=7025832">this Java bug</a> which is marked "will not fix".
 *
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @see <a href="https://github.com/adrianmsmith/databasesandlife-java-common">Project on GitHub</a>
 */
public class UuidTreeSet extends TreeSet<UUID> {

    public static class UuidComparator implements Comparator<UUID> {
        @Override public int compare(UUID o1, UUID o2) {
            return o1.toString().compareTo(o2.toString());
        }
    }

    public UuidTreeSet() {
        super(new UuidComparator());
    }

    public UuidTreeSet(Collection<? extends UUID> values) {
        this();
        addAll(values);
    }
}
