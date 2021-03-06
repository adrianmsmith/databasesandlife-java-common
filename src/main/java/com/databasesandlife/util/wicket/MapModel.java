package com.databasesandlife.util.wicket;

import java.util.Map;

import org.apache.wicket.model.IModel;

/**
 * Model based on a map.
 *
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @see <a href="https://github.com/adrianmsmith/databasesandlife-java-common">Project on GitHub</a>
 */
@SuppressWarnings("serial")
public class MapModel<K, V> implements IModel<V> {
    
    Map<K,V> map;
    K property;
    
    public MapModel(Map<K,V> map, K property) {
        this.map = map;
        this.property = property;
    }

    @Override public void detach() { }
    @Override public V getObject() { return map.get(property); }
    @Override public void setObject(V object) { map.put(property, object); }

}
