package com.databasesandlife.util.jooq;

import org.apache.commons.lang3.LocaleUtils;
import org.jooq.Converter;

import java.util.Locale;

@SuppressWarnings({ "serial" })
public class LocaleConverter implements Converter<String, Locale> {
    
    @Override public Class<String> fromType() { return String.class; }
    @Override public Class<Locale> toType() { return Locale.class; }

    @Override
    public Locale from(String x) {
        if (x == null) return null;
        return LocaleUtils.toLocale(x);
    }

    @Override
    public String to(Locale m) {
        if (m == null) return null;
        return m.toString();
    }
}
