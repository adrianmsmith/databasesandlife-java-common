package com.databasesandlife.util.jooq;

import com.databasesandlife.util.BCryptPassword;
import org.jooq.Converter;

import java.time.ZoneId;

@SuppressWarnings("serial")
public class BCryptPasswordConverter implements Converter<String, BCryptPassword> {

    @Override public Class<String> fromType() { return String.class; }
    @Override public Class<BCryptPassword> toType() { return BCryptPassword.class; }

    @Override
    public BCryptPassword from(String x) {
        if (x == null) return null;
        return new BCryptPassword(x);
    }

    @Override
    public String to(BCryptPassword x) {
        if (x == null) return null;
        return x.bcrypt;
    }
}
