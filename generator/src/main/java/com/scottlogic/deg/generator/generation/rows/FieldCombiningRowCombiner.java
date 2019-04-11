package com.scottlogic.deg.generator.generation.rows;

import com.scottlogic.deg.generator.generation.GenerationConfig;

import java.util.List;
import java.util.stream.Stream;

/**
 * Given a list of streams of rows, each list referring to a single field.
 *  return a single stream of rows with all the different fields added
 */
public class FieldCombiningRowCombiner implements RowCombiner {
    private final List<Stream<Row>> subGenerators;

    FieldCombiningRowCombiner(List<Stream<Row>> subGenerators) {
        this.subGenerators = subGenerators;
    }

    @Override
    public Stream<Row> generate(GenerationConfig generationConfig) {
        return generationConfig.getCombinationStrategy()
            .permute(subGenerators.stream());
    }
}
