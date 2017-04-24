package com.databasesandlife.util.wicket;

import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.validator.AbstractRangeValidator;

/**
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @see <a href="https://github.com/adrianmsmith/databasesandlife-java-common">Project on GitHub</a>
 */
@SuppressWarnings("serial")
public class MaxWordCountValidator extends AbstractRangeValidator<Integer, String> {
    
    public MaxWordCountValidator(int maxWords) {
        super(null, maxWords);
    }

    @Override protected Integer getValue(IValidatable<String> x) {
        return x.getValue().split("\\s+").length;
    }
}
