package com.databasesandlife.util.wicket;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.wicket.model.IModel;

/**
 * Wraps an underlying model, but sets a pre-defined list in the case the user doesn't enter anything.
 *   <p>
 * If the user enters greater than zero options, they are passed to the wrapped model.
 * Otherwise, if the user doesn't enter any options, some "default" list is set in the wrapped model.
 * The "default" list may be null. 
 *
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @see <a href="https://github.com/adrianmsmith/databasesandlife-java-common">Project on GitHub</a>
 */
@SuppressWarnings("serial")
public class EmptyListIsSpecialWrapperModel<T> implements IModel<List<T>> {
    protected List<T> emptyValues;
    protected IModel<List<T>> dataModel;
    
    /** @param e which values to be saved in the underlying model, in the case the user doesn't enter anything */
    public EmptyListIsSpecialWrapperModel(List<T> e, IModel<List<T>> m) { 
        emptyValues = e;
        dataModel=m; 
    }
    
    @Override public void detach() { }
    
    @Override public void setObject(List<T> userData) { 
        dataModel.setObject(userData.isEmpty() ? emptyValues : userData); 
    }
    
    /** Returns data to user i.e. show the user empty list, if model = empty values */
    @Override public List<T> getObject() {
        List<T> dataValues = dataModel.getObject();
        if (dataValues == null && emptyValues == null) return new ArrayList<T>();
        if (dataValues == null || emptyValues == null) return dataValues; // not the same if only one is null
        if (new HashSet<T>(dataValues).equals(new HashSet<T>(emptyValues))) return new ArrayList<T>();
        return dataValues;
    }
}
