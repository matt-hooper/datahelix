package com.scottlogic.deg.generator.generation.rows;

import com.scottlogic.deg.generator.generation.GenerationConfig;

import java.util.stream.Stream;

class DummyRowCombiner implements RowCombiner
{
    private final Stream<Row> rows;

    public DummyRowCombiner(Row... rows) {
        this.rows = Stream.of(rows);
    }

    @Override
    public Stream<Row> generate(GenerationConfig generationConfig) {
        return this.rows;
    }
}
