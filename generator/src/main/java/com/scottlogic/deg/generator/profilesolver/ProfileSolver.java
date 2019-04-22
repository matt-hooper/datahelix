package com.scottlogic.deg.generator.profilesolver;

import com.google.inject.Inject;
import com.scottlogic.deg.generator.Profile;
import com.scottlogic.deg.generator.decisiontree.DecisionTree;
import com.scottlogic.deg.generator.decisiontree.DecisionTreeFactory;
import com.scottlogic.deg.generator.decisiontree.DecisionTreeOptimiser;
import com.scottlogic.deg.generator.decisiontree.treepartitioning.TreePartitioner;
import com.scottlogic.deg.generator.generation.DataGenerator;
import com.scottlogic.deg.generator.generation.databags.GeneratedObject;

import java.util.stream.Stream;

public class ProfileSolver {

    private final DecisionTreeFactory decisionTreeGenerator;
    private final DataGenerator dataGenerator;
    private final TreePartitioner treePartitioner;
    private final DecisionTreeOptimiser treeOptimiser;

    @Inject
    public ProfileSolver(
        DataGenerator dataGenerator,
        DecisionTreeFactory decisionTreeGenerator,
        TreePartitioner treePartitioner,
        DecisionTreeOptimiser treeOptimiser) {
        this.dataGenerator = dataGenerator;
        this.decisionTreeGenerator = decisionTreeGenerator;
        this.treePartitioner = treePartitioner;
        this.treeOptimiser = treeOptimiser;
    }

    public Stream<GeneratedObject> generateData(Profile profile){
        final DecisionTree tree = decisionTreeGenerator.analyse(profile).getMergedTree();

        final Stream<Stream<GeneratedObject>> partitionedGeneratedObjects = treePartitioner
                .splitTreeIntoPartitions(tree)
                .map(treeOptimiser::optimiseTree)
                .map(partitionedTree -> dataGenerator.generateData(profile, partitionedTree));

        return treePartitioner.combinePartitions(partitionedGeneratedObjects);
    }
}
