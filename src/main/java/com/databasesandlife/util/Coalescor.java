package com.databasesandlife.util;

/**
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @see <a href="https://github.com/adrianmsmith/databasesandlife-java-common">Project on GitHub</a>
 */
public class Coalescor {

    @SafeVarargs 
    public static<T> T coalesce(T... values) {
        for (int i = 0; i < values.length; i++) {
            if (values[i] != null) return values[i];
        }
        return null;
    }

}
