package com.databasesandlife.util.jooq;

import org.jooq.Converter;

@SuppressWarnings({ "serial" })
public class MySqlBooleanConverter implements Converter<Byte, Boolean> {

    @Override public Class<Byte> fromType() { return Byte.class; }
    @Override public Class<Boolean> toType() { return Boolean.class; }

    @Override
    public Boolean from(Byte x) {
        if (x == null) return null;
        if (x == (byte)0) return false;
        return true;
    }

    @Override
    public Byte to(Boolean m) {
        if (m == null) return null;
        return m ? (byte)1 : 0;
    }
}
