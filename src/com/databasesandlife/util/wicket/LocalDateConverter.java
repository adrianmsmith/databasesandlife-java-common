package com.databasesandlife.util.wicket;

import java.util.Locale;

import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

@SuppressWarnings("serial")
public class LocalDateConverter implements IConverter<LocalDate> {

    private final String pattern;

    public LocalDateConverter(String pattern) {
        this.pattern = pattern;
    }

    private DateTimeFormatter getFormatter(Locale locale) {
        return DateTimeFormat.forPattern(pattern).withLocale(locale);
    }

    @Override
    public LocalDate convertToObject(String value, Locale locale) {
        try {
            return LocalDate.parse(value, getFormatter(locale));
        } catch (final RuntimeException e) {
            throw new ConversionException(e.getMessage(), e);
        }
    }

    @Override
    public String convertToString(LocalDate value, Locale locale) {
        return value == null ? "" : value.toString(getFormatter(locale));
    }
}