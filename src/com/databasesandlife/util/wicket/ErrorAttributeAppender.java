package com.databasesandlife.util.wicket;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.model.Model;

public class ErrorAttributeAppender extends AttributeAppender {

    private static final long serialVersionUID = 1L;
    private Boolean componentHasError;

    public ErrorAttributeAppender() {
        super("class", Model.of(" error"));
    }

    @Override
    protected String newValue(String currentValue, String appendValue) {
        if (componentHasError) {
            return super.newValue(currentValue, appendValue);
        }else{
            return currentValue.replaceAll(appendValue, "");
        }
    }

    @Override
    public void onConfigure(Component component) {
        componentHasError = component.hasErrorMessage();
        super.onConfigure(component);
    }

}
