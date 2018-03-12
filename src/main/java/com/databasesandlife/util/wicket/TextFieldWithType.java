package com.databasesandlife.util.wicket;

import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;

import javax.annotation.Nonnull;

public class TextFieldWithType<T> extends TextField<T> {

    protected final @Nonnull String type;

    public TextFieldWithType(@Nonnull String wicketId, @Nonnull String type, @Nonnull IModel<T> model) {
        super(wicketId, model);
        this.type = type;
    }

    // Wicket <= 6
    @Override
    protected String getInputType() {
        return type;
    }

    // Wicket >= 7
    protected String[] getInputTypes() {
        return new String[] { getInputType() };
    }
}
