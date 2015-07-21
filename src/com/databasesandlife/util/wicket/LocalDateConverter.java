package com.databasesandlife.util.wicket;

import java.util.Locale;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.lang.Args;

/**
 * Converter to convert String to LocalTime and vice versa.
 *
 * @author Reko Jokelainen / Nitor Creations
 */
public class LocalDateConverter implements IConverter<LocalDate> {

    private final String pattern;

    public LocalDateConverter(String pattern) {
        this.pattern = Args.notNull(pattern, "Pattern");
    }

    private DateTimeFormatter getFormatter() {
        return DateTimeFormat.forPattern(pattern);
    }

    @Override
    public LocalDate convertToObject(String value, Locale locale) {
        try {
            return LocalDate.parse(value, getFormatter());
        } catch (final RuntimeException e) {
            throw new ConversionException(e.getMessage(), e);
        }
    }

    @Override
    public String convertToString(LocalDate value, Locale locale) {
        return value == null ? "" : value.toString(getFormatter());
    }

    public String getPattern() {
        return pattern;
    }

}