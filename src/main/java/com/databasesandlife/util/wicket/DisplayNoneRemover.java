package com.databasesandlife.util.wicket;

import org.apache.wicket.AttributeModifier;

/**
 * Removes "display:none" from an HTML tag. 
 *
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @see <a href="https://github.com/adrianmsmith/databasesandlife-java-common">Project on GitHub</a>
 */
@SuppressWarnings("serial")
public class DisplayNoneRemover extends AttributeModifier {
    public DisplayNoneRemover() { super("style", ""); }
    @Override protected String newValue(final String currentValue, final String replacementValue) {
        if (currentValue == null) return null;
        return currentValue.replaceAll("display:\\s*none", "");
    }
}

