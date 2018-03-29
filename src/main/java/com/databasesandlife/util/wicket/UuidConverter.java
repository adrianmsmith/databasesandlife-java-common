package com.databasesandlife.util.wicket;

import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.Locale;
import java.util.UUID;

/**
 * Wicket converter to allow user to enter UUIDs and display errors if they are not valid.
 *
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @see <a href="https://github.com/adrianmsmith/databasesandlife-java-common">Project on GitHub</a>
 */
public class UuidConverter implements IConverter<UUID> {

    @Override public @CheckForNull UUID convertToObject(@CheckForNull String value, @Nonnull Locale locale)
    throws ConversionException {
        if (value == null) return null;

        try { return UUID.fromString(value); }
        catch (IllegalArgumentException e) {
            throw new ConversionException("Cannot understand "+UuidConverter.class+" '" + value + "'")
                .setSourceValue(value)
                .setResourceKey("UuidConverter.IllegalArgumentException")
                .setTargetType(UuidConverter.class)
                .setConverter(this)
                .setLocale(locale);
        }
    }

    @Override public @CheckForNull String convertToString(@CheckForNull UUID value, @Nonnull Locale locale) {
        if (value == null) return null;
        return value.toString();
    }
}
