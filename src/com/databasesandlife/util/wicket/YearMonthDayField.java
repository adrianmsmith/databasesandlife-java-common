package com.databasesandlife.util.wicket;

import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;

import com.databasesandlife.util.YearMonthDay;

/** An HTML5 &lt;input type="date"&gt; */
@SuppressWarnings("serial")
public class YearMonthDayField extends TextField<YearMonthDay> {
    
    public YearMonthDayField(String wicketId, IModel<YearMonthDay> model) {
        super(wicketId, model);
    }
    
    @Override protected String getInputType() {
        return "date";
    }
}
