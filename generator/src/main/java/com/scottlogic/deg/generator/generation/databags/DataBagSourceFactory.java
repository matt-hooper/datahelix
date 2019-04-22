package com.scottlogic.deg.generator.generation.databags;

import com.google.inject.Inject;
import com.scottlogic.deg.generator.DataBagValue;
import com.scottlogic.deg.generator.Field;
import com.scottlogic.deg.generator.fieldspecs.FieldSpec;
import com.scottlogic.deg.generator.fieldspecs.RowSpec;
import com.scottlogic.deg.generator.generation.FieldSpecValueGenerator;
import com.scottlogic.deg.generator.generation.combinationstrategies.CombinationStrategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

public class DataBagSourceFactory {
    private final FieldSpecValueGenerator dataBagValueGenerator;
    private final CombinationStrategy combinationStrategy;

    @Inject
    public DataBagSourceFactory(FieldSpecValueGenerator dataBagValueGenerator, CombinationStrategy combinationStrategy) {
        this.dataBagValueGenerator = dataBagValueGenerator;
        this.combinationStrategy = combinationStrategy;
    }

    public DataBagSource createDataBagSource(RowSpec rowSpec){
        List<Stream<GeneratedObject>> fieldDataBagSources = new ArrayList<>();

        for (Field field: rowSpec.getFields()) {
            FieldSpec fieldSpec = rowSpec.getSpecForField(field);

            fieldDataBagSources.add(
                dataBagValueGenerator.generate(field, fieldSpec)
                    .map(this::toGeneratedObject)
            );
        }

        return new FieldCombiningDataBagSource(fieldDataBagSources, combinationStrategy);
    }

    private GeneratedObject toGeneratedObject(DataBagValue dataBagValue) {
        return new GeneratedObject(new HashMap<Field, DataBagValue>() {{
            put(dataBagValue.field, dataBagValue); }});
    }
}
