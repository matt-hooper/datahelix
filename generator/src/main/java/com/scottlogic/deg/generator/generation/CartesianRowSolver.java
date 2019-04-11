package com.scottlogic.deg.generator.generation;

import com.google.inject.Inject;
import com.scottlogic.deg.generator.Profile;
import com.scottlogic.deg.generator.decisiontree.DecisionTree;
import com.scottlogic.deg.generator.fieldspecs.RowSpec;
import com.scottlogic.deg.generator.generation.rows.ConcatenatingRowCombiner;
import com.scottlogic.deg.generator.generation.rows.RowCombiner;
import com.scottlogic.deg.generator.generation.rows.Row;
import com.scottlogic.deg.generator.generation.rows.RowCombinerFactory;
import com.scottlogic.deg.generator.walker.DecisionTreeWalker;

import java.util.stream.Stream;

/**
 * Generates data by walking a tree to get RowSpecs, then creating data from the RowSpecs
 */
public class CartesianRowSolver implements RowSolver {

    private final DecisionTreeWalker treeWalker;
    private final RowCombinerFactory rowCombinerFactory;
    private final GenerationConfig generationConfig;

    @Inject
    public CartesianRowSolver(
        DecisionTreeWalker treeWalker,
        RowCombinerFactory rowCombinerFactory,
        GenerationConfig generationConfig) {
        this.treeWalker = treeWalker;
        this.rowCombinerFactory = rowCombinerFactory;
        this.generationConfig = generationConfig;
    }

    @Override
    public Stream<Row> generateRows(Profile profile, DecisionTree analysedProfile) {
        Stream<RowSpec> rowSpecs = treeWalker.walk(analysedProfile);

        return generateDataFromRowSpecs(rowSpecs);
    }

    private Stream<Row> generateDataFromRowSpecs(Stream<RowSpec> rowSpecs) {
        Stream<RowCombiner> rowCombinerStream = rowSpecs.map(rowCombinerFactory::createRowCombiner);

        return new ConcatenatingRowCombiner(rowCombinerStream)
            .generate(generationConfig);
    }
}
