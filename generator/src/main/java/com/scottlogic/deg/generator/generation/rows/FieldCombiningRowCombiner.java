package com.scottlogic.deg.generator.generation.rows;

import com.scottlogic.deg.generator.generation.GenerationConfig;
import com.scottlogic.deg.generator.generation.combinationstrategies.CombinationStrategy;

import java.util.List;
import java.util.stream.Stream;

/**
 * Given a list of streams of rows, each list referring to a single field.
 *  return a single stream of rows with all the different fields added
 */
public class FieldCombiningRowCombiner implements RowCombiner {
    private final List<Stream<Row>> subGenerators;
    private final CombinationStrategy combinationStrategy;

    FieldCombiningRowCombiner(List<Stream<Row>> subGenerators, CombinationStrategy combinationStrategy) {
        this.subGenerators = subGenerators;
        this.combinationStrategy = combinationStrategy;
    }

    @Override
    public Stream<Row> generate(GenerationConfig generationConfig) {
        return combinationStrategy.permute(subGenerators.stream());
    }
}
