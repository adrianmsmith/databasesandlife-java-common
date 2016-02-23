package com.databasesandlife.util.wicket;

import java.util.Set;

import org.apache.wicket.model.IModel;

/**
 * A model modeling "true" or "false" based upon whether an element is in a Set.
 *
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @version $Revision: 6666 $
 */
@SuppressWarnings("serial")
public class BooleanSetModel<E> implements IModel<Boolean> {
    final protected Set<E> set;
    final E element;
    public BooleanSetModel(Set<E> s, E e) { set=s; element=e; }
    @Override public void detach() {  }
    @Override public Boolean getObject() { return set.contains(element); }
    @Override public void setObject(Boolean x) { if (x) set.add(element); else set.remove(element); }
}

