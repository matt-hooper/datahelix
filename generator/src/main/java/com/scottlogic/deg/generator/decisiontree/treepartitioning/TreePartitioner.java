package com.scottlogic.deg.generator.decisiontree.treepartitioning;

import com.scottlogic.deg.generator.decisiontree.DecisionTree;
import com.scottlogic.deg.generator.generation.databags.GeneratedObject;

import java.util.stream.Stream;

public interface TreePartitioner {
    Stream<DecisionTree> splitTreeIntoPartitions(DecisionTree decisionTree);
    Stream<GeneratedObject> combinePartitions(Stream<Stream<GeneratedObject>> partitionedStreams);
}
