package com.databasesandlife.util.wicket;

import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.validator.AbstractRangeValidator;

@SuppressWarnings("serial")
public class MaxWordCountValidator extends AbstractRangeValidator<Integer, String> {
    
    public MaxWordCountValidator(int maxWords) {
        super(null, maxWords);
    }

    @Override protected Integer getValue(IValidatable<String> x) {
        return x.getValue().split("\\s+").length;
    }
}
