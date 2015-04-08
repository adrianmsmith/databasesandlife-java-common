package com.databasesandlife.util;

public class Coalescor {

    @SafeVarargs 
    public static<T> T coalesce(T... values) {
        for (int i = 0; i < values.length; i++) {
            if (values[i] != null) return values[i];
        }
        return null;
    }

}
