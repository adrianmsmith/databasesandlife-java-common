package com.databasesandlife.util.wicket;

import com.databasesandlife.util.gwtsafe.CleartextPassword;
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;

import java.util.Locale;

/**
 * Wicket Converter for {@link CleartextPassword}.
 *
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @see <a href="https://github.com/adrianmsmith/databasesandlife-java-common">Project on GitHub</a>
 */
public class CleartextPasswordConverter implements IConverter<CleartextPassword> {

    @Override public CleartextPassword convertToObject(String value, Locale locale)
    throws ConversionException {
        if (value == null) return null;
        return new CleartextPassword(value);
    }

    @Override public String convertToString(CleartextPassword value, Locale locale) {
        if (value == null) return null;
        return value.getCleartext();
    }
}
