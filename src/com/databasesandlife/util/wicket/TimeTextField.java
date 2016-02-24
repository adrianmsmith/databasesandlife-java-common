package com.databasesandlife.util.wicket;

import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;

/**
 * &lt;input type="time"&gt; field.
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
    protected String getInputType()
    {
        return "time";
    }

}
