package com.scottlogic.deg.generator.generation;

import com.scottlogic.deg.generator.Field;
import com.scottlogic.deg.generator.fieldspecs.FieldSpec;
import com.scottlogic.deg.generator.fieldspecs.RowSpec;
import com.scottlogic.deg.generator.generation.databags.GeneratedObject;
import com.scottlogic.deg.generator.walker.reductive.ReductiveState;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class SystemOutDataGeneratorMonitor implements ReductiveDataGeneratorMonitor {

    @Override
    public void generationStarting(GenerationConfig generationConfig) {
    }

    @Override
    public void rowEmitted(GeneratedObject row) {
    }

    @Override
    public void endGeneration() {
    }

    @Override
    public void rowSpecEmitted(RowSpec rowSpec) {
        System.out.println("RowSpec emitted");
    }

    @Override
    public void fieldFixedToValue(Field field, Object current) {
        System.out.println(String.format("Field [%s] = %s", field.name, current));
    }

    @Override
    public void unableToStepFurther(ReductiveState reductiveState) {
        System.out.println(
            String.format(
                "%d: Unable to step further %s ",
                reductiveState.getFieldValues().size(),
                reductiveState.toString(true)));
    }

    @Override
    public void noValuesForField(ReductiveState reductiveState, Field field) {
        System.out.println(
            String.format(
                "%d: No values for field %s: %s ",
                reductiveState.getFieldValues().size(),
                field,
                reductiveState.toString(true)));
    }

    @Override
    public void unableToEmitRowAsSomeFieldSpecsAreEmpty(ReductiveState reductiveState, Map<Field, FieldSpec> fieldSpecsPerField) {
        List<Map.Entry<Field, FieldSpec>> emptyFieldSpecs = fieldSpecsPerField.entrySet()
            .stream()
            .filter(entry -> entry.getValue() == FieldSpec.Empty)
            .collect(Collectors.toList());

        System.out.println(
            String.format(
                "%d: Unable to emit row, some FieldSpec's are Empty: %s",
                reductiveState.getFieldValues().size(),
                Objects.toString(emptyFieldSpecs)));
    }
}

