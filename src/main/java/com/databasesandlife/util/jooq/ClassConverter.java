package com.databasesandlife.util.jooq;

import org.apache.log4j.Logger;
import org.jooq.Converter;

@SuppressWarnings({ "serial", "rawtypes" })
public class ClassConverter implements Converter<String, Class> {

    @Override public Class<String> fromType() { return String.class; }
    @Override public Class<Class> toType() { return Class.class; }

    @Override
    public Class from(String str) {
        if (str == null) return null;
        try { return Class.forName(str); }
        catch (ClassNotFoundException e) {
            Logger.getLogger(getClass()).warn("Class '"+str+"' not found", e);
            return null;
        }
    }

    @Override
    public String to(Class m) {
        if (m == null) return null;
        return m.getName();
    }
}
