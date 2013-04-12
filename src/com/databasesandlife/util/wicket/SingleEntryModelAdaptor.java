package com.databasesandlife.util.wicket;

import java.util.Arrays;
import java.util.List;

import org.apache.wicket.model.IModel;

/**
 * Wraps a model of a single value, in a model capable of storing a list of values.
 *    <p>
 * Multiple-entry text fields can allow the user to select multiple values.
 * However, if they have a "non-multiple" version, the user can only select one value.
 * The appropriate model for such a field is a Model&lt;X> but the multiple-entry text field will
 * require a Model&lt;List&lt;X>>. 
 *    <p>
 * An object of this class allows the application to provide a Model&lt;X> but the multiple-value
 * text field (which is operating in single-value mode) to see a Model&lt;List&lt;X>>.
 */
public class SingleEntryModelAdaptor<T> implements IModel<List<T>> {
    
    protected IModel<T> singleEntryModel;
    
    public SingleEntryModelAdaptor(IModel<T> x) { 
        singleEntryModel = x;
    }

    @Override public void detach() { 
        singleEntryModel.detach(); 
    }

    @SuppressWarnings("unchecked")
    @Override public List<T> getObject() {
        return Arrays.asList(singleEntryModel.getObject()); 
    }

    @Override
    public void setObject(List<T> newList) {
        if (newList == null || newList.isEmpty()) singleEntryModel.setObject(null);
        else if (newList.size() == 1) singleEntryModel.setObject(newList.get(0));
        else throw new IllegalArgumentException("Expected <= 1 elements, found " + newList.size());
    }
}
