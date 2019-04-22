package com.scottlogic.deg.generator;

import com.google.inject.Inject;
import com.scottlogic.deg.generator.decisiontree.DecisionTreeFactory;
import com.scottlogic.deg.generator.generation.*;
import com.scottlogic.deg.generator.generation.databags.GeneratedObject;
import com.scottlogic.deg.generator.outputs.targets.OutputTarget;
import com.scottlogic.deg.generator.profilesolver.ProfileSolver;

import java.io.IOException;
import java.util.stream.Stream;

public class StandardGenerationEngine implements GenerationEngine {
    private ReductiveDataGeneratorMonitor monitor;
    private final ProfileSolver profileSolver;

    @Inject
    public StandardGenerationEngine(ReductiveDataGeneratorMonitor monitor, ProfileSolver profileSolver) {
        this.monitor = monitor;
        this.profileSolver = profileSolver;
    }

    public void generateDataSet(Profile profile, GenerationConfig config, OutputTarget outputTarget) throws IOException {
        Stream<GeneratedObject> generatedDataItems = profileSolver.generateData(profile)
            .limit(config.getMaxRows().orElse(GenerationConfig.Constants.DEFAULT_MAX_ROWS))
            .peek(this.monitor::rowEmitted);

        monitor.generationStarting(config);
        outputTarget.outputDataset(generatedDataItems, profile.fields);
        monitor.endGeneration();
    }
}
