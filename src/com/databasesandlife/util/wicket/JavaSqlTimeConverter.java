package com.databasesandlife.util.wicket;

import java.sql.Time;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/** Displays java.sql.Time as produced by jOOQ in PostgreSQL which are adjusted to the local timezone of the server */
@SuppressWarnings("serial")
public class JavaSqlTimeConverter implements IConverter<Time> {

    private final String pattern;

    public JavaSqlTimeConverter(String pattern) {
        this.pattern = pattern;
    }

    private DateTimeFormatter getFormatter(Locale locale) {
        return DateTimeFormat.forPattern(pattern).withLocale(locale).withZoneUTC();
    }

    @Override
    public Time convertToObject(String value, Locale locale) {
        try {
            return new Time(LocalTime.parse(value, getFormatter(locale)).getMillisOfDay()
                - TimeZone.getDefault().getOffset(new Date().getTime()));
        } catch (final RuntimeException e) {
            throw new ConversionException(e.getMessage(), e);
        }
    }

    @Override
    public String convertToString(Time value, Locale locale) {
        return value == null ? "" : LocalTime.fromMillisOfDay(value.getTime() 
            + TimeZone.getDefault().getOffset(new Date().getTime())).toString(getFormatter(locale));
    }
}