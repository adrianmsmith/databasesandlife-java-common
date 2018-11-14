package com.databasesandlife.util.wicket;

import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.IModel;
import org.danekja.java.util.function.serializable.SerializableFunction;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.util.List;

public class LambdaDisplayValueChoiceRenderer<T> implements IChoiceRenderer<T> {
    public final @Nonnull SerializableFunction<T, String> supplier;

    public LambdaDisplayValueChoiceRenderer(@Nonnull SerializableFunction<T, String> supplier) {
        this.supplier = supplier;
    }

    @Override public String getDisplayValue(@CheckForNull T object) {
        if (object == null) return null;
        else return supplier.apply(object);
    }

    @Override public String getIdValue(T object, int index) {
        return Integer.toString(index);
    }

    @Override public T getObject(String id, IModel<? extends List<? extends T>> choices) {
        try {
            return choices.getObject().get(Integer.parseInt(id));
        }
        catch (NumberFormatException e) {
            // For example, user selected "Please choose" which returns id ""
            return null;
        }
    }
}
