package com.databasesandlife.util.wicket;

import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.IConverter;

/**
 * &lt;input type="date"&gt; field. 
 * 
 * <p>Model may be {@link java.sql.Date} or Java 8 {@link java.time.LocalDate}.
 * (Jodatime is not supported as this library is Java 8 and thus Java 8 time should be used in preference.) 
 * 
 * @author The Java source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @version $Revision$
 */
@SuppressWarnings("serial")
public class DateTextField<T> extends TextField<T> {

    public DateTextField(String id, IModel<T> model) {
        super(id, model);
    }

    @Override
    protected String getInputType() {
        return "date";
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <C> IConverter<C> getConverter(Class<C> type) {
        if (type.equals(Date.class)) return (IConverter<C>) new IConverter<Date>() {
            @Override public Date convertToObject(String str, Locale l) {
                if (str == null) return null;
                return Date.valueOf(LocalDate.parse(str, DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            }

            @Override public String convertToString(Date time, Locale l) {
                if (time == null) return null;
                return time.toLocalDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            }
        };

        if (type.equals(LocalDate.class)) return (IConverter<C>) new IConverter<LocalDate>() {
            @Override public LocalDate convertToObject(String str, Locale l) { 
                return LocalDate.parse(str, DateTimeFormatter.ofPattern("yyyy-MM-dd")); }
            @Override public String convertToString(LocalDate time, Locale l) { 
                return time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")); }
        };

        return super.getConverter(type);
    }
}
