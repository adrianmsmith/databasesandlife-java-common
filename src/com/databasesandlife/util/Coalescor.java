package com.databasesandlife.util;

public class Coalescor {

    public static<T> T coalesce(@SuppressWarnings("unchecked") T... values) {
        for (int i = 0; i < values.length; i++) {
            if (values[i] != null) return values[i];
        }
        return null;
    }

}
