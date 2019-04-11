package com.scottlogic.deg.generator.generation.rows;

import com.google.inject.Inject;
import com.scottlogic.deg.generator.Field;
import com.scottlogic.deg.generator.fieldspecs.FieldSpec;
import com.scottlogic.deg.generator.fieldspecs.RowSpec;
import com.scottlogic.deg.generator.generation.ValueGenerator;
import com.scottlogic.deg.generator.generation.combinationstrategies.CombinationStrategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

public class RowFactory {
    private final ValueGenerator valueGenerator;
    private final CombinationStrategy combinationStrategy;

    @Inject
    public RowFactory(ValueGenerator valueGenerator, CombinationStrategy combinationStrategy) {
        this.valueGenerator = valueGenerator;
        this.combinationStrategy = combinationStrategy;
    }

    public Stream<Row> createFromRowSpec(RowSpec rowSpec){
        List<Stream<Row>> fieldRowSources = new ArrayList<>();

        for (Field field: rowSpec.getFields()) {
            FieldSpec fieldSpec = rowSpec.getSpecForField(field);

            fieldRowSources.add(
                valueGenerator.generate(field, fieldSpec)
                    .map(this::toRow)
            );
        }
        return combinationStrategy.permute(fieldRowSources.stream());
    }

    private Row toRow(Value value) {
        HashMap<Field, Value> fieldValue = new HashMap<>();
        fieldValue.put(value.field, value);
        return new Row(fieldValue);
    }
}
