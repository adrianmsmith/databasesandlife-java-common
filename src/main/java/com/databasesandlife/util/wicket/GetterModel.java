package com.databasesandlife.util.wicket;

import org.apache.wicket.model.IModel;

/**
 * @deprecated use {@link org.apache.wicket.model.AbstractReadOnlyModel} instead
 */
@SuppressWarnings("serial")
public abstract class GetterModel<T> implements IModel<T> {

    @Override public void detach() { }
    @Override public void setObject(T object) { throw new UnsupportedOperationException(); }
}
