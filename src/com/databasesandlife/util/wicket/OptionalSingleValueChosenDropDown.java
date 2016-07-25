package com.databasesandlife.util.wicket;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.ListMultipleChoice;
import org.apache.wicket.model.IModel;

/**
 * Drop-down using "chosen" JS library, only zero or one options may be selected
 * <p>
 * This is a hack. Wicket has a DropDownChoice but it always insists on
 * providing a "Please choose..." text as the first entry, in case the model is
 * null. We don't want that, as this is done in Javascript. HOWEVER: the
 * multiple="true" means we get multiple-select which we don't want. AND: If
 * there is no "" option then the "X" doesn't appear, so we allow that as a
 * valid option
 *
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 */
@SuppressWarnings("serial")
public class OptionalSingleValueChosenDropDown extends ListMultipleChoice<String> {
    
    protected class EmptyStringIntroducingModel implements IModel<List<String>> {
        protected IModel<String> singleStringOrNullModel;
        EmptyStringIntroducingModel(IModel<String> m) { singleStringOrNullModel = m; }
        @Override public void detach() { }
        @Override public List<String> getObject() {
            String value = singleStringOrNullModel.getObject();
            if (value == null) value = "";
            return new ArrayList<String>(Arrays.asList(value));
        }
        @Override public void setObject(List<String> newList) {
            String newValue = newList.get(0).isEmpty() ? null : newList.get(0);
            singleStringOrNullModel.setObject(newValue);
        }
    }
    
    public OptionalSingleValueChosenDropDown(String wicketId, IModel<String> model, List<String> choices) {
        super(wicketId);
        
        setDefaultModel(new EmptyStringIntroducingModel(model));
        
        List<String> choicesAndEmptyString = new ArrayList<String>(choices);
        choicesAndEmptyString.add(0, "");
        setChoices(choicesAndEmptyString);
        add(new Behavior() {
            @Override public void onComponentTag(final Component component, final ComponentTag tag) {
                tag.getAttributes().remove("multiple");
            }
        });
    }

}
