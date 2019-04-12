package com.scottlogic.deg.generator.generation;

import com.scottlogic.deg.generator.Profile;
import com.scottlogic.deg.generator.decisiontree.DecisionTree;
import com.scottlogic.deg.generator.generation.rows.Row;

import java.util.stream.Stream;

public interface RowGenerator {
    Stream<Row> generateRows(Profile profile, DecisionTree analysedProfile);
}
