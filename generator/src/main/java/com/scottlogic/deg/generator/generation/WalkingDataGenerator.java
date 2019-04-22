package com.scottlogic.deg.generator.generation;

import com.google.inject.Inject;
import com.scottlogic.deg.generator.FlatMappingSpliterator;
import com.scottlogic.deg.generator.Profile;
import com.scottlogic.deg.generator.decisiontree.DecisionTree;
import com.scottlogic.deg.generator.fieldspecs.RowSpec;
import com.scottlogic.deg.generator.generation.databags.DataBagSource;
import com.scottlogic.deg.generator.generation.databags.GeneratedObject;
import com.scottlogic.deg.generator.generation.databags.DataBagSourceFactory;
import com.scottlogic.deg.generator.walker.DecisionTreeWalker;

import java.util.stream.Stream;

/**
 * Generates data by walking a tree to get RowSpecs, then creating data from the RowSpecs
 */
public class WalkingDataGenerator implements DataGenerator {

    private final DecisionTreeWalker treeWalker;
    private final DataBagSourceFactory dataBagSourceFactory;
    private final GenerationConfig generationConfig;

    @Inject
    public WalkingDataGenerator(
        DecisionTreeWalker treeWalker,
        DataBagSourceFactory dataBagSourceFactory,
        GenerationConfig generationConfig) {
        this.treeWalker = treeWalker;
        this.dataBagSourceFactory = dataBagSourceFactory;
        this.generationConfig = generationConfig;
    }

    @Override
    public Stream<GeneratedObject> generateData(Profile profile, DecisionTree analysedProfile) {
        Stream<RowSpec> rowSpecs = treeWalker.walk(analysedProfile);

        return generateDataFromRowSpecs(rowSpecs);
    }

    private Stream<GeneratedObject> generateDataFromRowSpecs(Stream<RowSpec> rowSpecs) {
        Stream<DataBagSource> dataBagSources = rowSpecs.map(dataBagSourceFactory::createDataBagSource);

        return FlatMappingSpliterator.flatMap(
            dataBagSources
                .map(source -> source.generate(generationConfig)),
            streamOfStreams -> streamOfStreams);
    }
}
