package com.scottlogic.deg.generator.generation.databags;

import com.scottlogic.deg.generator.*;
import com.scottlogic.deg.generator.fieldspecs.FieldSpecSource;
import com.scottlogic.deg.generator.outputs.CellSource;
import com.scottlogic.deg.generator.outputs.RowSource;

import java.util.*;
import java.util.stream.Collectors;


public class GeneratedObject {
    public static final GeneratedObject empty = new GeneratedObject(new HashMap<>());

    private final Map<Field, DataBagValue> fieldToValue;
    private final ProfileFields fieldOrdering;

    public GeneratedObject(Map<Field, DataBagValue> fieldToValue) {
        this(fieldToValue, null);
    }

    private GeneratedObject(Map<Field, DataBagValue> fieldToValue, ProfileFields fieldOrdering){
        this.fieldToValue = fieldToValue;
        this.fieldOrdering = fieldOrdering;
    }

    public Map<Field, DataBagValue> getFieldToValue() {
        return fieldToValue;
    }

    public Collection<DataBagValue> getValues() {
        if (fieldOrdering == null) {
            return fieldToValue.values();
        }

        return fieldOrdering.stream()
            .map(fieldToValue::get)
            .collect(Collectors.toList());
    }

    public GeneratedObject withOrdering(ProfileFields fieldOrdering){
        return new GeneratedObject(fieldToValue, fieldOrdering);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GeneratedObject generatedObject = (GeneratedObject) o;
        return Objects.equals(fieldToValue, generatedObject.fieldToValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fieldToValue);
    }
}
