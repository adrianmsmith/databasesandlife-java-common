package com.databasesandlife.util.jooq;

import org.jooq.Converter;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

@SuppressWarnings("serial")
public class InternetAddressConverter implements Converter<String, InternetAddress> {

    @Override public Class<String> fromType() { return String.class; }
    @Override public Class<InternetAddress> toType() { return InternetAddress.class; }

    @Override
    public InternetAddress from(String x) {
        try {
            if (x == null) return null;
            return new InternetAddress(x);
        }
        catch (AddressException e) { throw new RuntimeException(e); }
    }

    @Override
    public String to(InternetAddress x) {
        if (x == null) return null;
        return x.getAddress();
    }
}
