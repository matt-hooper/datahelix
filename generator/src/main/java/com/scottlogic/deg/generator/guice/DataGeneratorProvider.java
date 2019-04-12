package com.scottlogic.deg.generator.guice;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.scottlogic.deg.generator.decisiontree.DecisionTreeOptimiser;
import com.scottlogic.deg.generator.decisiontree.treepartitioning.TreePartitioner;
import com.scottlogic.deg.generator.generation.*;
import com.scottlogic.deg.generator.generation.combinationstrategies.CombinationStrategy;
import com.scottlogic.deg.generator.walker.RestartingRowGenerator;
import com.scottlogic.deg.generator.walker.ReductiveRowGenerator;

public class DataGeneratorProvider implements Provider<RowGenerator> {

    private final CartesianRowGenerator cartesianRowGenerator;
    private final ReductiveRowGenerator reductiveRowGenerator;

    private final GenerationConfigSource configSource;

    private final TreePartitioner treePartitioner;
    private final DecisionTreeOptimiser optimiser;
    private final CombinationStrategy combinationStrategy;

    @Inject
    public DataGeneratorProvider(CartesianRowGenerator cartesianRowGenerator,
                                 ReductiveRowGenerator reductiveRowGenerator,
                                 GenerationConfigSource configSource,
                                 TreePartitioner treePartitioner,
                                 DecisionTreeOptimiser optimiser,
                                 CombinationStrategy combinationStrategy){
        this.cartesianRowGenerator = cartesianRowGenerator;
        this.reductiveRowGenerator = reductiveRowGenerator;
        this.configSource = configSource;
        this.treePartitioner = treePartitioner;
        this.optimiser = optimiser;
        this.combinationStrategy = combinationStrategy;
    }

    @Override
    public RowGenerator get() {
        boolean isReductive = configSource.getWalkerType() == GenerationConfig.TreeWalkerType.REDUCTIVE;
        boolean isRandom = configSource.getGenerationType() == GenerationConfig.DataGenerationType.RANDOM;

        RowGenerator generator = isReductive
            ? reductiveRowGenerator
            : cartesianRowGenerator;

        if (configSource.shouldDoPartitioning()) {
            generator = decorateWithPartitioning(generator);
        }

        if (isRandom && isReductive) {
            //restarting should be the outermost step if used with partitioning.
            generator = decorateWithRestarting(generator);
        }

        return generator;
    }

    private RowGenerator decorateWithPartitioning(RowGenerator underlying) {
        return new PartitioningRowSolver(underlying, treePartitioner, optimiser, combinationStrategy);
    }

    private RowGenerator decorateWithRestarting(RowGenerator underlying) {
        return new RestartingRowGenerator(underlying);
    }
}
