package com.databasesandlife.util;

/**
 * @deprecated Use {@link java.util.Optional} instead, as its {@link java.util.Optional#orElse(Object)} method
 *     returns a non-null object that the IntelliJ can verify as non-null.
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
