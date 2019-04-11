package com.scottlogic.deg.generator.guice;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.scottlogic.deg.generator.generation.GenerationConfig;
import com.scottlogic.deg.generator.generation.combinationstrategies.*;

public class CombinationStrategyProvider implements Provider<CombinationStrategy> {
    private final GenerationConfig config;

    @Inject
    public CombinationStrategyProvider(GenerationConfig config){
        this.config = config;
    }

    @Override
    public CombinationStrategy get() {
        if (config.getWalkerType() == GenerationConfig.TreeWalkerType.REDUCTIVE){
            return new ReductiveCombinationStrategy();
        }

        switch(config.getCombinationStrategy()){
            case EXHAUSTIVE: return new ExhaustiveCombinationStrategy();
            case PINNING: return new PinningCombinationStrategy();
            case MINIMAL: return new MinimalCombinationStrategy();
            default:
                throw new UnsupportedOperationException(
                    "$Combination strategy {this.combinationStrategy} is unsupported.");
        }
    }
}
