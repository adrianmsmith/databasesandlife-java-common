package com.databasesandlife.util.wicket;

import java.sql.Time;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;

/**
 * <code>&lt;input type="time"&gt;</code> field. 
 * 
 * <p>Model may be {@link java.sql.Time} or Java 8 {@link java.time.LocalTime}.
 * (Jodatime is not supported as this library is Java 8 and thus Java 8 time should be used in preference.) 
 * 
 * <p>For those browsers which do not support <code>&lt;input type="time"&gt;</code>, 
 * which display a normal <code>&lt;input type="text"&gt;</code>
 * instead, for example Firefox at the time of writing, we show a placeholder text, and display an error in case the format is wrong.
 * 
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @see <a href="https://github.com/adrianmsmith/databasesandlife-java-common">Project on GitHub</a>
 */
@SuppressWarnings("serial")
public class TimeTextField<T> extends TextField<T> {

    public TimeTextField(String id, IModel<T> model) {
        super(id, model);
    }

    @Override
    protected String getInputType() {
        return "time";
    }
    
    protected LocalTime parse(String str) {
        try {
            if (str == null) return null;
            else return LocalTime.parse(str, DateTimeFormatter.ofPattern("HH:mm"));
        }
        catch (DateTimeParseException e) { 
            throw new ConversionException(e).setResourceKey("TimeTextField.invalid").setSourceValue(str);
        }
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <C> IConverter<C> getConverter(Class<C> type) {
        if (type.equals(Time.class)) return (IConverter<C>) new IConverter<Time>() {
            @Override public Time convertToObject(String str, Locale l) { 
                if (str == null) return null;
                return Time.valueOf(parse(str)); 
            }
            @Override public String convertToString(Time time, Locale l) {
                if (time == null) return null;
                return time.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"));
            }
        };

        if (type.equals(LocalTime.class)) return (IConverter<C>) new IConverter<LocalTime>() {
            @Override public LocalTime convertToObject(String str, Locale l) { 
                return parse(str); 
            }
            @Override public String convertToString(LocalTime time, Locale l) { 
                if (time == null) return null;
                return time.format(DateTimeFormatter.ofPattern("HH:mm")); 
            }
        };

        return super.getConverter(type);
    }
    
    @Override
    protected void onComponentTag(ComponentTag tag) {
        super.onComponentTag(tag);
        String placeholder = (String) tag.getAttributes().get("placeholder");
        if (placeholder == null) tag.getAttributes().put("placeholder", "HH:MM");
    }
}
