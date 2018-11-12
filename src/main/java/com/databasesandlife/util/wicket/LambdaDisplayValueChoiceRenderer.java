package com.databasesandlife.util.wicket;

import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.IModel;
import org.danekja.java.util.function.serializable.SerializableFunction;

import java.util.List;

public class LambdaDisplayValueChoiceRenderer<T> implements IChoiceRenderer<T> {
    public final SerializableFunction<T, String> supplier;

    public LambdaDisplayValueChoiceRenderer(SerializableFunction<T, String> supplier) {
        this.supplier = supplier;
    }

    @Override public Object getDisplayValue(T object) {
        return supplier.apply(object);
    }

    @Override public String getIdValue(T object, int index) {
        return Integer.toString(index);
    }

    @Override public T getObject(String id, IModel<? extends List<? extends T>> choices) {
        return choices.getObject().get(Integer.parseInt(id));
    }
}
