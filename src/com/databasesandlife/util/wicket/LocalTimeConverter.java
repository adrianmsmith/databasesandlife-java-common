package com.databasesandlife.util.wicket;

import java.util.Locale;

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
public class LocalTimeConverter implements IConverter<LocalTime> {

    private static final long serialVersionUID = 1L;

    public static final String DEFAULT_PATTERN = "HH:MM";

    private final String pattern;

    public LocalTimeConverter() {
        this(DEFAULT_PATTERN);
    }

    public LocalTimeConverter(String pattern) {
        this.pattern = Args.notNull(pattern, "Pattern");
    }

    private DateTimeFormatter getFormatter() {
        return DateTimeFormat.forPattern(pattern);
    }

    @Override
    public LocalTime convertToObject(String value, Locale locale) {
        try {
            return LocalTime.parse(value, getFormatter());
        } catch (final RuntimeException e) {
            throw new ConversionException(e.getMessage(), e);
        }
    }

    @Override
    public String convertToString(LocalTime value, Locale locale) {
        return value == null ? "" : value.toString(getFormatter());
    }

    public String getPattern() {
        return pattern;
    }

}