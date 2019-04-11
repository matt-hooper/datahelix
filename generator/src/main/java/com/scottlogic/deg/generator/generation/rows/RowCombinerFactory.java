package com.scottlogic.deg.generator.generation.rows;

import com.google.inject.Inject;
import com.scottlogic.deg.generator.Field;
import com.scottlogic.deg.generator.fieldspecs.FieldSpec;
import com.scottlogic.deg.generator.fieldspecs.RowSpec;
import com.scottlogic.deg.generator.generation.ValueGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

public class RowCombinerFactory {
    private final ValueGenerator valueGenerator;

    @Inject
    public RowCombinerFactory(ValueGenerator valueGenerator) {
        this.valueGenerator = valueGenerator;
    }

    public RowCombiner createRowCombiner(RowSpec rowSpec){
        List<Stream<Row>> fieldFowSources = new ArrayList<>();

        for (Field field: rowSpec.getFields()) {
            FieldSpec fieldSpec = rowSpec.getSpecForField(field);

            fieldFowSources.add(
                valueGenerator.generate(field, fieldSpec)
                    .map(this::toRow)
            );
        }

        return new FieldCombiningRowCombiner(fieldFowSources);
    }

    private Row toRow(Value value) {
        HashMap<Field, Value> fieldValue = new HashMap<>();
        fieldValue.put(value.field, value);
        return new Row(fieldValue);
    }
}
