package com.databasesandlife.util.wicket;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.PropertyModel;

public class InlineCalendarViewDateField extends FormComponentPanel<Date> {
    
    protected SimpleDateFormat dateFormatter;
    protected String selectedDate;  // YYYY-MM-DD
    protected TextField<String> dateField;
    
    public InlineCalendarViewDateField(String wicketId) {
        super(wicketId);
        
        dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        dateFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));
        
        dateField = new TextField<String>("selectedDate", new PropertyModel<String>(this, "selectedDate"));
        add(dateField);
    }

    @Override protected void onBeforeRender() {
        Date currentDate = getModelObject();
        selectedDate = dateFormatter.format(currentDate);
        
        super.onBeforeRender();
    }
    
    @Override protected void convertInput() {
        try {
            String newDateStr = dateField.getConvertedInput();
            Date newDate = dateFormatter.parse(newDateStr);
            setConvertedInput(newDate);
        }
        catch (ParseException e) { throw new RuntimeException(e); } // can never happen; date field is set by JS calendar object
    }
}
