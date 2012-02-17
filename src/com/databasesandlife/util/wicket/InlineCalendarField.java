package com.databasesandlife.util.wicket;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

import com.databasesandlife.util.YearMonthDay;

/**
 * Allows the user to select a date using a calendar.
 *    <p>
 * The date is mandatory (as the calendar is always displayed).
 *    <p>
 * In your HTML, use <code>&lt;div wicket:id="xxxx"&gt;&lt;/div&gt;</code>.
 * You need to include JQuery UI.
 * You need to include a JQuery UI CSS using the <a href="http://jqueryui.com/themeroller/">JQuery UI ThemeRoller</a>.
 *
 * @author The Java source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @version $Revision: 2637 $
 */
public class InlineCalendarField extends FormComponentPanel<YearMonthDay> {
    
    protected String jsId;
    protected TextField<String> textField;
    protected String textFieldValue;
    
    protected void addWidgets() {
        jsId = getId().replace(".", "-");
        
        WebMarkupContainer translationInclude = new WebMarkupContainer("translations") {
            // there is no English "translation", as this is the default language
            public boolean isVisible() { return !getLocale().getLanguage().equals("en"); };
        };
        translationInclude.add(new AttributeModifier("src", new PropertyModel<String>(this, "translationScriptSrc")));
        add(translationInclude);
        
        textField = new TextField<String>("input", new PropertyModel<String>(this, "textFieldValue"));
        textField.add(new SimpleAttributeModifier("style", "display:none"));
        textField.add(new SimpleAttributeModifier("id", "input" + jsId));
        add(textField);
        
        Label script = new Label("script");
        script.setEscapeModelStrings(false);
        script.setDefaultModel(new PropertyModel<String>(this, "scriptContents"));
        add(script);

        WebMarkupContainer div = new WebMarkupContainer("div");
        div.add(new SimpleAttributeModifier("id", "div" + jsId));
        add(div);
    }
    
    public String getTranslationScriptSrc() {
        String language = getLocale().getLanguage();
        return "http://ajax.googleapis.com/ajax/libs/jqueryui/1.8.1/i18n/jquery.ui.datepicker-"+language+".min.js";
    }
    
    public String getScriptContents() {
        String language = getLocale().getLanguage();
        return "var wicketId = '" + jsId + "'; var language = '" + language + "';";
    }

    public InlineCalendarField(String id) {
        super(id);
        addWidgets();
    }

    public InlineCalendarField(String id, IModel<YearMonthDay> model) {
        super(id, model);
        addWidgets();
    }
    
    @Override protected void onBeforeRender() {
        super.onBeforeRender();
        textFieldValue = getModelObject().toYYYYMMDD();
    }
    
    @Override protected void convertInput() {
        super.convertInput();
        setConvertedInput(YearMonthDay.newForYYYYMMDD(textField.getConvertedInput()));
    }
}
