package com.scottlogic.deg.generator.generation.databags;

import com.scottlogic.deg.generator.generation.GenerationConfig;
import com.scottlogic.deg.generator.generation.combinationstrategies.CombinationStrategy;

import java.util.List;
import java.util.stream.Stream;

/**
 * Given a list of streams of GeneratedObjects, each list referring to a single field.
 *  return a single stream of GeneratedObjects with all the different fields added
 */
public class FieldCombiningDataBagSource implements DataBagSource {
    private final List<Stream<GeneratedObject>> subGenerators;
    private final CombinationStrategy combinationStrategy;

    FieldCombiningDataBagSource(List<Stream<GeneratedObject>> subGenerators, CombinationStrategy combinationStrategy) {
        this.subGenerators = subGenerators;
        this.combinationStrategy = combinationStrategy;
    }

    @Override
    public Stream<GeneratedObject> generate(GenerationConfig generationConfig) {
        return combinationStrategy.permute(subGenerators.stream());
    }
}
