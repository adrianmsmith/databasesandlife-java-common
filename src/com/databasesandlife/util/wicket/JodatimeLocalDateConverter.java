package com.databasesandlife.util.wicket;

import java.util.Locale;

import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * @deprecated use {@link DateTextField} instead
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @see <a href="https://github.com/adrianmsmith/databasesandlife-java-common">Project on GitHub</a>
 */
@SuppressWarnings("serial")
public class JodatimeLocalDateConverter implements IConverter<LocalDate> {

    private final String pattern;

    public JodatimeLocalDateConverter(String pattern) {
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