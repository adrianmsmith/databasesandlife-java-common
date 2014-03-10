package com.databasesandlife.util.wicket;

import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;

import com.databasesandlife.util.YearMonthDay;

/** 
 * An HTML5 &lt;input type="date"&gt; 
 *
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @version $Revision$
 */
@SuppressWarnings("serial")
public class YearMonthDayField extends TextField<YearMonthDay> {
    
    public YearMonthDayField(String wicketId) { super(wicketId, YearMonthDay.class); }
    public YearMonthDayField(String wicketId, IModel<YearMonthDay> model) { super(wicketId, model, YearMonthDay.class); }
    
    @Override protected String getInputType() {
        return "date";
    }
}
