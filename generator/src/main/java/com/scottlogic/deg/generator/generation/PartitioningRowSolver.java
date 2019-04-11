package com.scottlogic.deg.generator.generation;

import com.scottlogic.deg.generator.Profile;
import com.scottlogic.deg.generator.decisiontree.DecisionTree;
import com.scottlogic.deg.generator.decisiontree.DecisionTreeOptimiser;
import com.scottlogic.deg.generator.decisiontree.treepartitioning.TreePartitioner;
import com.scottlogic.deg.generator.generation.combinationstrategies.CombinationStrategy;
import com.scottlogic.deg.generator.generation.rows.*;

import java.util.stream.Stream;

/**
 * Splits a Tree into subtrees, Generates data for each tree
 * Combines the data for each tree returns a stream of fully generated data
 */
public class PartitioningRowSolver implements RowSolver {
    private final TreePartitioner treePartitioner;
    private final DecisionTreeOptimiser treeOptimiser;
    private final RowSolver innerGenerator;
    private final CombinationStrategy combinationStrategy;

    public PartitioningRowSolver(
        RowSolver innerGenerator,
        TreePartitioner treePartitioner,
        DecisionTreeOptimiser optimiser,
        CombinationStrategy combinationStrategy) {
        this.treePartitioner = treePartitioner;
        this.treeOptimiser = optimiser;
        this.innerGenerator = innerGenerator;
        this.combinationStrategy = combinationStrategy;
    }

    @Override
    public Stream<Row> generateRows(Profile profile, DecisionTree decisionTree) {;

        final Stream<Stream<Row>> partitionedRows =
            treePartitioner
                .splitTreeIntoPartitions(decisionTree)
                .map(this.treeOptimiser::optimiseTree)
                .map(tree -> innerGenerator.generateRows(profile, tree));

        return combinationStrategy.permute(partitionedRows);
    }
}
