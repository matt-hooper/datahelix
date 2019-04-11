package com.scottlogic.deg.generator.guice;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.scottlogic.deg.generator.decisiontree.DecisionTreeOptimiser;
import com.scottlogic.deg.generator.decisiontree.treepartitioning.TreePartitioner;
import com.scottlogic.deg.generator.generation.*;
import com.scottlogic.deg.generator.generation.combinationstrategies.CombinationStrategy;
import com.scottlogic.deg.generator.walker.RestartingRowSolver;
import com.scottlogic.deg.generator.walker.ReductiveRowSolver;

public class DataGeneratorProvider implements Provider<RowSolver> {

    private final CartesianRowSolver cartesianRowSolver;
    private final ReductiveRowSolver reductiveRowSolver;

    private final GenerationConfigSource configSource;

    private final TreePartitioner treePartitioner;
    private final DecisionTreeOptimiser optimiser;
    private final CombinationStrategy combinationStrategy;

    @Inject
    public DataGeneratorProvider(CartesianRowSolver cartesianRowSolver,
                                 ReductiveRowSolver reductiveRowSolver,
                                 GenerationConfigSource configSource,
                                 TreePartitioner treePartitioner,
                                 DecisionTreeOptimiser optimiser,
                                 CombinationStrategy combinationStrategy){
        this.cartesianRowSolver = cartesianRowSolver;
        this.reductiveRowSolver = reductiveRowSolver;
        this.configSource = configSource;
        this.treePartitioner = treePartitioner;
        this.optimiser = optimiser;
        this.combinationStrategy = combinationStrategy;
    }

    @Override
    public RowSolver get() {
        boolean isReductive = configSource.getWalkerType() == GenerationConfig.TreeWalkerType.REDUCTIVE;
        boolean isRandom = configSource.getGenerationType() == GenerationConfig.DataGenerationType.RANDOM;

        RowSolver generator = isReductive
            ? reductiveRowSolver
            : cartesianRowSolver;

        if (configSource.shouldDoPartitioning()) {
            generator = decorateWithPartitioning(generator);
        }

        if (isRandom && isReductive) {
            //restarting should be the outermost step if used with partitioning.
            generator = decorateWithRestarting(generator);
        }

        return generator;
    }

    private RowSolver decorateWithPartitioning(RowSolver underlying) {
        return new PartitioningRowSolver(underlying, treePartitioner, optimiser, combinationStrategy);
    }

    private RowSolver decorateWithRestarting(RowSolver underlying) {
        return new RestartingRowSolver(underlying);
    }
}
