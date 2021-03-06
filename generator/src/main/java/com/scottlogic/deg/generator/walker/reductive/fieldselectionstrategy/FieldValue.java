package com.scottlogic.deg.generator.walker.reductive.fieldselectionstrategy;

import com.scottlogic.deg.generator.Field;
import com.scottlogic.deg.generator.fieldspecs.FieldSpec;
import com.scottlogic.deg.generator.fieldspecs.FieldSpecSource;
import com.scottlogic.deg.generator.restrictions.FormatRestrictions;

public class FieldValue {
    private final Field field;
    private final Object value;
    private final FieldSpecSource valueSource;
    private final FormatRestrictions formatRestrictions;

    public FieldValue(Field field, Object value){
        this(field, value, FieldSpec.Empty);
    }
    public FieldValue(Field field, Object value, FieldSpec valueSource){
        this.value = value;
        this.field = field;
        this.valueSource = valueSource.getFieldSpecSource();
        this.formatRestrictions = valueSource.getFormatRestrictions();
    }

    public Object getValue() {
        return value;
    }

    public Field getField() {
        return field;
    }

    @Override
    public String toString() {
        if (value == null) return "null";
        return value.toString();
    }

    public FormatRestrictions getFormatRestrictions() {
        return formatRestrictions;
    }

    public FieldSpecSource getFieldSpecSource() {
        return valueSource;
    }
}
