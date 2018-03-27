package com.databasesandlife.util.wicket;

import org.apache.wicket.model.AbstractWrapModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

/**
 * Takes an underlying model which returns a map, and returns that map's keys
 *
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @see <a href="https://github.com/adrianmsmith/databasesandlife-java-common">Project on GitHub</a>
 */
public class MapKeyModel<K extends Comparable<?>> extends AbstractWrapModel<List<K>> {

    final @Nonnull IModel<Map<K,?>> wrappedModel;

    public MapKeyModel(@Nonnull IModel<Map<K,?>> wrappedModel) {
        this.wrappedModel = wrappedModel;
    }

    /** Convenience method which wraps a {@link org.apache.wicket.model.PropertyModel} */
    public MapKeyModel(@Nonnull Object modelObject, @Nonnull String expression) {
        this(new PropertyModel<>(modelObject, expression));
    }

    @Override public IModel<Map<K,?>> getWrappedModel() {
        return wrappedModel;
    }

    @Override public List<K> getObject() {
        return getWrappedModel().getObject().keySet().stream().sorted().collect(toList());
    }
}
