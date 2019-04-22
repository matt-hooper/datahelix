package com.scottlogic.deg.generator.walker.reductive;

import com.google.inject.Inject;
import com.scottlogic.deg.generator.DataBagValue;
import com.scottlogic.deg.generator.Field;
import com.scottlogic.deg.generator.fieldspecs.*;
import com.scottlogic.deg.generator.generation.ReductiveDataGeneratorMonitor;
import com.scottlogic.deg.generator.restrictions.FormatRestrictions;

import java.util.*;
import java.util.stream.Collectors;

public class ReductiveRowSpecGenerator {

    private final FieldSpecHelper fieldSpecHelper;
    private final ReductiveDataGeneratorMonitor monitor;

    @Inject
    public ReductiveRowSpecGenerator(
        FieldSpecHelper fieldSpecHelper,
        ReductiveDataGeneratorMonitor monitor) {
        this.fieldSpecHelper = fieldSpecHelper;
        this.monitor = monitor;
    }

    //produce a RowSpec for each value in the permitted set of values for the field fixed on the last iteration
    public RowSpec createRowSpecsFromFixedValues(ReductiveState reductiveState) {
        Map<Field, FieldSpec> fieldSpecsPerField =
            reductiveState.getFieldValues().values().stream()
                .collect(Collectors.toMap(
                    DataBagValue::getField,
                    fieldValue -> fieldSpecHelper.getFieldSpecForValue(fieldValue)
                                    .withFormatRestrictions(new FormatRestrictions(fieldValue.getFormat()), FieldSpecSource.Empty)));//TODO PAUL

        RowSpec rowSpec = new RowSpec(reductiveState.getFields(), fieldSpecsPerField);
        monitor.rowSpecEmitted(rowSpec);
        return rowSpec;
    }
}
