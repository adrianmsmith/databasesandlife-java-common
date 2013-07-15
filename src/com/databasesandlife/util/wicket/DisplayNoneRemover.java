package com.databasesandlife.util.wicket;

import org.apache.wicket.AttributeModifier;

@SuppressWarnings("serial")
public class DisplayNoneRemover extends AttributeModifier {
    public DisplayNoneRemover() { super("style", ""); }
    @Override protected String newValue(final String currentValue, final String replacementValue) {
        if (currentValue == null) return null;
        return currentValue.replaceAll("display:\\s*none", "");
    }
}

