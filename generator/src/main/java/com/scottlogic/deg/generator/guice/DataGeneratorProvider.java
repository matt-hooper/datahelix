package com.scottlogic.deg.generator.guice;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.scottlogic.deg.generator.decisiontree.DecisionTreeOptimiser;
import com.scottlogic.deg.generator.decisiontree.treepartitioning.TreePartitioner;
import com.scottlogic.deg.generator.generation.*;
import com.scottlogic.deg.generator.walker.RestartingDataGeneratorDecorator;
import com.scottlogic.deg.generator.walker.ReductiveDataGenerator;

public class DataGeneratorProvider implements Provider<DataGenerator> {

    private final WalkingDataGenerator walkingDataGenerator;
    private final ReductiveDataGenerator reductiveDataGenerator;

    private final GenerationConfigSource configSource;

    @Inject
    public DataGeneratorProvider(WalkingDataGenerator walkingDataGenerator,
                                 ReductiveDataGenerator reductiveDataGenerator,
                                 GenerationConfigSource configSource){
        this.walkingDataGenerator = walkingDataGenerator;
        this.reductiveDataGenerator = reductiveDataGenerator;
        this.configSource = configSource;
    }

    @Override
    public DataGenerator get() {
        boolean isReductive = configSource.getWalkerType() == GenerationConfig.TreeWalkerType.REDUCTIVE;
        boolean isRandom = configSource.getGenerationType() == GenerationConfig.DataGenerationType.RANDOM;

        DataGenerator generator = isReductive
            ? reductiveDataGenerator
            : walkingDataGenerator;

        if (isRandom && isReductive) {
            generator = decorateWithRestarting(generator);
        }

        return generator;
    }

    private DataGenerator decorateWithRestarting(DataGenerator underlying) {
        return new RestartingDataGeneratorDecorator(underlying);
    }
}
