package com.databasesandlife.util.wicket;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.model.IModel;

/**
 * Wraps a model of a single value, in a model capable of storing a list of values.
 *    <p>
 * Multiple-entry text fields can allow the user to select multiple values.
 * However, if they have a "non-multiple" version, the user can only select one value.
 * The appropriate model for such a field is a Model&lt;X&gt; but the multiple-entry text field will
 * require a Model&lt;List&lt;X&gt;&gt;. 
 *    <p>
 * An object of this class allows the application to provide a Model&lt;X&gt; but the multiple-value
 * text field (which is operating in single-value mode) to see a Model&lt;List&lt;X&gt;&gt;.
 *
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @see <a href="https://github.com/adrianmsmith/databasesandlife-java-common">Project on GitHub</a>
 */
@SuppressWarnings("serial")
public class SingleEntryModelAdaptor<T> implements IModel<List<T>> {
    
    protected IModel<T> singleEntryModel;
    
    public SingleEntryModelAdaptor(IModel<T> x) { 
        singleEntryModel = x;
    }

    @Override public void detach() { 
        singleEntryModel.detach(); 
    }

    @Override public List<T> getObject() {
        // return Arrays.asList(..) doesn't work:
        // the resulting array is actually MODIFIED by Wicket's Select object before being passed to setObject
        T object = singleEntryModel.getObject();
        List<T> result = new ArrayList<>();
        if (object != null) result.add(object);
        return result;
    }

    @Override public void setObject(List<T> newList) {
        if (newList == null || newList.isEmpty()) singleEntryModel.setObject(null);
        else if (newList.size() == 1) singleEntryModel.setObject(newList.get(0));
        else throw new IllegalArgumentException("Expected <= 1 elements, found " + newList.size());
    }
}
