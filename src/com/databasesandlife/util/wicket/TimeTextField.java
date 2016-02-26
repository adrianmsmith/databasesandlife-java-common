package com.databasesandlife.util.wicket;

import java.sql.Time;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.IConverter;

/**
 * &lt;input type="time"&gt; field. 
 * 
 * <p>Model may be {@link java.sql.Time} or Java 8 {@link java.time.LocalTime}.
 * (Jodatime is not supported as this library is Java 8 and thus Java 8 time should be used in preference.) 
 * 
 * @author The Java source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @version $Revision: 8157 $
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
    
    @SuppressWarnings("unchecked")
    @Override
    public <C> IConverter<C> getConverter(Class<C> type) {
        if (type.equals(Time.class)) return (IConverter<C>) new IConverter<Time>() {
            @Override public Time convertToObject(String str, Locale l) {
                if (str == null) return null;
                return Time.valueOf(LocalTime.parse(str, DateTimeFormatter.ofPattern("HH:mm")));
            }

            @Override public String convertToString(Time time, Locale l) {
                if (time == null) return null;
                return time.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"));
            }
        };

        if (type.equals(LocalTime.class)) return (IConverter<C>) new IConverter<LocalTime>() {
            @Override public LocalTime convertToObject(String str, Locale l) { 
                return LocalTime.parse(str, DateTimeFormatter.ofPattern("HH:mm")); }
            @Override public String convertToString(LocalTime time, Locale l) { 
                return time.format(DateTimeFormatter.ofPattern("HH:mm")); }
        };

        return super.getConverter(type);
    }
}
