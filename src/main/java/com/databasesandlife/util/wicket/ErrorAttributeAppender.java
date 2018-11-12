package com.databasesandlife.util.wicket;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.model.Model;

import java.io.Serializable;

/**
 *
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @see <a href="https://github.com/adrianmsmith/databasesandlife-java-common">Project on GitHub</a>
 */
public class ErrorAttributeAppender extends AttributeAppender {

    private static final long serialVersionUID = 1L;
    private Boolean componentHasError;

    public ErrorAttributeAppender() {
        super("class", Model.of(" error"));
    }

    @Override
    protected Serializable newValue(String currentValue, String appendValue) {
        if (componentHasError) {
            return super.newValue(currentValue, appendValue);
        }else{
            if(currentValue!=null){
                return currentValue.replaceAll(appendValue, "");
            }else{
                return "";
            }
        }
    }

    @Override
    public void onConfigure(Component component) {
        componentHasError = component.hasErrorMessage();
        super.onConfigure(component);
    }

}
