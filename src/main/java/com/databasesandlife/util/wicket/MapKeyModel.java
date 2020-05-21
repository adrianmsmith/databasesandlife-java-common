package com.databasesandlife.util.wicket;

import org.apache.wicket.model.AbstractWrapModel;
import org.apache.wicket.model.IModel;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

/**
 * Takes an underlying model which returns a map, and returns that map's keys.
 *   <p>
 *       <b>Performance note:</b> 
 *       Do not use this in a ListView if there are a large number of entries (e.g. more than 1,000).
 *       Unfortunately the model's getObject method gets called a great number of times if it's used as a ListView's model,
 *       and the populateItem method calls getModelObject, which does a listView.getModel().get(i).
 *   </p>
 *
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @see <a href="https://github.com/adrianmsmith/databasesandlife-java-common">Project on GitHub</a>
 */
public class MapKeyModel<K extends Comparable<?>> extends AbstractWrapModel<List<K>> {

    final @Nonnull IModel<SortedMap<K,?>> wrappedModel;

    public MapKeyModel(@Nonnull IModel<SortedMap<K,?>> wrappedModel) {
        this.wrappedModel = wrappedModel;
    }

    @Override public IModel<SortedMap<K,?>> getWrappedModel() {
        return wrappedModel;
    }

    @Override public List<K> getObject() {
        return new ArrayList<>(getWrappedModel().getObject().keySet());
    }
}
