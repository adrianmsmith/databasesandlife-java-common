package com.databasesandlife.util.jooq;

import com.databasesandlife.util.jooq.CurrencyIso4217NumericCodeConverter.InvalidCurrencyException;
import org.jooq.Converter;

import java.util.Collection;
import java.util.Comparator;
import java.util.Currency;
import java.util.TreeSet;

import static com.databasesandlife.util.jooq.CurrencyIso4217NumericCodeConverter.lookup;
import static java.util.Arrays.asList;

@SuppressWarnings({ "serial" })
public class CurrencyIso4217NumericCodeSetConverter implements Converter<Integer[], CurrencyIso4217NumericCodeSetConverter.CurrencyIso4217NumericCodeSortedSet> {

    public static class CurrencyIso4217NumericCodeSortedSet extends TreeSet<Currency> {
        public CurrencyIso4217NumericCodeSortedSet() {
            super(Comparator.comparingInt(Currency::getNumericCode));
        }

        public CurrencyIso4217NumericCodeSortedSet(Collection<Integer> currencyIds) throws InvalidCurrencyException {
            this();
            for (int c : currencyIds) add(lookup(c));
        }
    }

    @Override public Class<Integer[]> fromType() { return Integer[].class; }
    @Override public Class<CurrencyIso4217NumericCodeSortedSet> toType() { return CurrencyIso4217NumericCodeSortedSet.class; }

    @Override
    public CurrencyIso4217NumericCodeSortedSet from(Integer[] x) {
        try {
            if (x == null) return null;
            return new CurrencyIso4217NumericCodeSortedSet(asList(x));
        }
        catch (InvalidCurrencyException e) { throw new RuntimeException(e); }
    }

    @Override
    public Integer[] to(CurrencyIso4217NumericCodeSortedSet m) {
        if (m == null) return null;
        return m.stream().map(Currency::getNumericCode).toArray(Integer[]::new);
    }
}
