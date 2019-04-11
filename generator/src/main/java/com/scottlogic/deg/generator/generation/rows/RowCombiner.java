package com.scottlogic.deg.generator.generation.rows;

import com.scottlogic.deg.generator.generation.GenerationConfig;

import java.util.stream.Stream;

public interface RowCombiner {
    Stream<Row> generate(GenerationConfig generationConfig);
}
