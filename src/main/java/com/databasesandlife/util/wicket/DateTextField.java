package com.databasesandlife.util.wicket;

import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;

/**
 * &lt;input type="date"&gt; field. 
 * 
 * <p>Model may be {@link java.sql.Date} or Java 8 {@link java.time.LocalDate}.
 * (Jodatime is not supported as this library is Java 8 and thus Java 8 time should be used in preference.) 
 * 
 * <p>For those browsers which do not support <code>&lt;input type="date"&gt;</code>, 
 * which display a normal <code>&lt;input type="text"&gt;</code>
 * instead, for example Firefox at the time of writing, we show a placeholder text, and display an error in case the format is wrong.
 * 
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @see <a href="https://github.com/adrianmsmith/databasesandlife-java-common">Project on GitHub</a>
 */
@SuppressWarnings("serial")
public class DateTextField<T> extends TextFieldWithType<T> {

    public DateTextField(String id, IModel<T> model) {
        super(id, "date", model);
    }

    protected LocalDate parse(String str) {
        try {
            if (str == null) return null;
            else return LocalDate.parse(str, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
        catch (DateTimeParseException e) { 
            throw new ConversionException(e)
                .setResourceKey("DateTextField.invalid")
                .setSourceValue(str)
                .setVariable("today", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <C> IConverter<C> getConverter(Class<C> type) {
        if (type.equals(Date.class)) return (IConverter<C>) new IConverter<Date>() {
            @Override public Date convertToObject(String str, Locale l) {
                if (str == null) return null;
                return Date.valueOf(parse(str));
            }

            @Override public String convertToString(Date time, Locale l) {
                if (time == null) return null;
                return time.toLocalDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            }
        };

        if (type.equals(LocalDate.class)) return (IConverter<C>) new IConverter<LocalDate>() {
            @Override public LocalDate convertToObject(String str, Locale l) { 
                return parse(str);
            }
            
            @Override public String convertToString(LocalDate time, Locale l) { 
                return time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")); 
            }
        };

        return super.getConverter(type);
    }
    
    @Override
    protected void onComponentTag(ComponentTag tag) {
        super.onComponentTag(tag);
        String placeholder = (String) tag.getAttributes().get("placeholder");
        if (placeholder == null) tag.getAttributes().put("placeholder", "YYYY-MM-DD");
    }
}
