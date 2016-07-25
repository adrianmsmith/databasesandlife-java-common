package com.databasesandlife.util.wicket;

import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;

/**
 * &lt;input type="tel"&gt; field.
 * 
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 */
@SuppressWarnings("serial")
public class TelTextField extends TextField<String> {

    public TelTextField(String id, IModel<String> model) {
        super(id, model);
    }

    @Override
    protected String getInputType()
    {
        return "tel";
    }

}
