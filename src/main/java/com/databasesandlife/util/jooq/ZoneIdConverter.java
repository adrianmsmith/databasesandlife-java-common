package com.databasesandlife.util.jooq;

import org.jooq.Converter;

import java.time.ZoneId;

@SuppressWarnings({ "serial" })
public class ZoneIdConverter implements Converter<String, ZoneId> {

    @Override public Class<String> fromType() { return String.class; }
    @Override public Class<ZoneId> toType() { return ZoneId.class; }

    @Override
    public ZoneId from(String x) {
        if (x == null) return null;
        return ZoneId.of(x);
    }

    @Override
    public String to(ZoneId m) {
        if (m == null) return null;
        return m.getId();
    }
}
