package com.databasesandlife.util.jooq;

import org.jooq.Converter;

import javax.annotation.Nonnull;
import java.util.Currency;

@SuppressWarnings({ "serial" })
public class CurrencyIso4217NumericCodeConverter implements Converter<Integer, Currency> {

    @Override public Class<Integer> fromType() { return Integer.class; }
    @Override public Class<Currency> toType() { return Currency.class; }
    
    public static class InvalidCurrencyException extends Exception {
        InvalidCurrencyException(String msg) { super(msg); }
    }
    
    public static @Nonnull Currency lookup(int x) throws InvalidCurrencyException {
        for(Currency c : Currency.getAvailableCurrencies()) {
            if(c.getNumericCode() == x) {
                return c;
            }
        }
        throw new InvalidCurrencyException("Unknown ISO 4217 numeric currency code: " + x);
    }

    @Override
    public Currency from(Integer x) {
        try {
            if (x == null) return null;
            return lookup(x);
        }
        catch (InvalidCurrencyException e) { throw new RuntimeException(e); }
    }

    @Override
    public Integer to(Currency m) {
        if (m == null) return null;
        return m.getNumericCode();
    }
}
