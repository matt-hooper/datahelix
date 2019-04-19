package com.scottlogic.deg.generator.generation;

import com.scottlogic.deg.generator.Profile;
import com.scottlogic.deg.generator.decisiontree.DecisionTree;
import com.scottlogic.deg.generator.decisiontree.DecisionTreeOptimiser;
import com.scottlogic.deg.generator.decisiontree.treepartitioning.TreePartitioner;
import com.scottlogic.deg.generator.generation.combinationstrategies.CombinationStrategy;
import com.scottlogic.deg.generator.generation.databags.*;

import java.util.stream.Stream;

/**
 * Splits a Tree into subtrees, Generates data for each tree
 * Combines the data for each tree returns a stream of fully generated data
 */
public class PartitioningDataGeneratorDecorator implements DataGenerator {
    private final TreePartitioner treePartitioner;
    private final DecisionTreeOptimiser treeOptimiser;
    private final DataGenerator innerGenerator;

    public PartitioningDataGeneratorDecorator(
        DataGenerator innerGenerator,
        TreePartitioner treePartitioner,
        DecisionTreeOptimiser optimiser) {
        this.treePartitioner = treePartitioner;
        this.treeOptimiser = optimiser;
        this.innerGenerator = innerGenerator;
    }

    @Override
    public Stream<GeneratedObject> generateData(Profile profile, DecisionTree decisionTree) {
        final Stream<Stream<GeneratedObject>> partitionedGeneratedObjects =
            treePartitioner
                .splitTreeIntoPartitions(decisionTree)
                .map(this.treeOptimiser::optimiseTree)
                .map(tree -> innerGenerator.generateData(profile, tree));

        return treePartitioner.combinePartitions(partitionedGeneratedObjects);
    }
}
