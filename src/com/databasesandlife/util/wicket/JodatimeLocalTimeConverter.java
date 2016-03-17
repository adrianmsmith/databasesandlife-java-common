package com.databasesandlife.util.wicket;

import java.util.Locale;

import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/** @deprecated use {@link TimeTextField} instead */
@SuppressWarnings("serial")
public class JodatimeLocalTimeConverter implements IConverter<LocalTime> {

    private final String pattern;

    public JodatimeLocalTimeConverter(String pattern) {
        this.pattern = pattern;
    }

    private DateTimeFormatter getFormatter(Locale locale) {
        return DateTimeFormat.forPattern(pattern).withLocale(locale);
    }

    @Override
    public LocalTime convertToObject(String value, Locale locale) {
        try {
            return LocalTime.parse(value, getFormatter(locale));
        } catch (final RuntimeException e) {
            throw new ConversionException(e.getMessage(), e);
        }
    }

    @Override
    public String convertToString(LocalTime value, Locale locale) {
        return value == null ? "" : value.toString(getFormatter(locale));
    }
}