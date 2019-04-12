package com.scottlogic.deg.generator.generation;

import com.google.inject.Inject;
import com.scottlogic.deg.generator.FlatMappingSpliterator;
import com.scottlogic.deg.generator.Profile;
import com.scottlogic.deg.generator.decisiontree.DecisionTree;
import com.scottlogic.deg.generator.fieldspecs.RowSpec;
import com.scottlogic.deg.generator.generation.rows.Row;
import com.scottlogic.deg.generator.generation.rows.RowFactory;
import com.scottlogic.deg.generator.walker.DecisionTreeWalker;

import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Generates data by walking a tree to get RowSpecs, then creating data from the RowSpecs
 */
public class CartesianRowGenerator implements RowGenerator {

    private final DecisionTreeWalker treeWalker;
    private final RowFactory rowFactory;

    @Inject
    public CartesianRowGenerator(
        DecisionTreeWalker treeWalker,
        RowFactory rowFactory) {
        this.treeWalker = treeWalker;
        this.rowFactory = rowFactory;
    }

    @Override
    public Stream<Row> generateRows(Profile profile, DecisionTree analysedProfile) {
        Stream<RowSpec> rowSpecs = treeWalker.walk(analysedProfile);

        return generateRows(rowSpecs);
    }

    private Stream<Row> generateRows(Stream<RowSpec> rowSpecs) {
        return FlatMappingSpliterator.flatMap(
            rowSpecs.map(rowFactory::createFromRowSpec),
            Function.identity());
    }
}
