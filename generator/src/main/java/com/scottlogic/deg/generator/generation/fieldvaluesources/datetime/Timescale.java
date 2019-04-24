package com.scottlogic.deg.generator.generation.fieldvaluesources.datetime;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.function.Function;

/**
 *  * Order of the enums are important. Ensure most fine is top, and each further down is increasingly coarse.
 * See getMostCoarse(Timescale, Timescale) for more info.
 *
 */
public enum Timescale {

    MILLIS("millis",
        current -> current.plusNanos(1_000),
        d -> OffsetDateTime.of(d.getYear(), d.getMonth().getValue(), d.getDayOfMonth(), d.getHour(), d.getMinute(), d.getSecond(), nanoToMilli(d.getNano()), ZoneOffset.UTC)),

    SECONDS("seconds",
        current -> current.plusSeconds(1),
        d -> OffsetDateTime.of(d.getYear(), d.getMonth().getValue(), d.getDayOfMonth(), d.getHour(), d.getMinute(), d.getSecond(), 0, ZoneOffset.UTC)),

    MINUTES("minutes",
        current -> current.plusMinutes(1),
        d -> OffsetDateTime.of(d.getYear(), d.getMonth().getValue(), d.getDayOfMonth(), d.getHour(), d.getMinute(), 0, 0, ZoneOffset.UTC)),

    HOURS("hours",
        current -> current.plusHours(1),
        d -> OffsetDateTime.of(d.getYear(), d.getMonth().getValue(), d.getDayOfMonth(), d.getHour(), 0, 0, 0, ZoneOffset.UTC)),

    DAYS("days",
        current -> current.plusDays(1),
        d -> OffsetDateTime.of(d.getYear(), d.getMonth().getValue(), d.getDayOfMonth(), 0, 0, 0, 0, ZoneOffset.UTC)),

    MONTHS("months",
        current -> current.plusMonths(1),
        d -> OffsetDateTime.of(d.getYear(), d.getMonth().getValue(), 1, 0, 0, 0, 0, ZoneOffset.UTC)),

    YEARS("years",
        current -> current.plusYears(1),
        d -> OffsetDateTime.of(d.getYear(), 1, 1, 0, 0, 0, 0, ZoneOffset.UTC));

    private final String name;

    private final Function<OffsetDateTime, OffsetDateTime> next;

    private final Function<OffsetDateTime, OffsetDateTime> granularityFunction;

    Timescale(final String name, final Function<OffsetDateTime, OffsetDateTime> next, final Function<OffsetDateTime, OffsetDateTime> granularityFunction) {
        this.name = name;
        this.next = next;
        this.granularityFunction = granularityFunction;
    }

    public static Timescale getMostCoarse(Timescale a, Timescale b) {
        if (a == null || b == null) {
            throw new IllegalArgumentException("Requires not null arguments.");
        }
        OffsetDateTime initial = OffsetDateTime.MIN;
        return a.getNext().apply(initial).compareTo(b.getNext().apply(initial)) <= 0 ? b : a;
    }

    public static Timescale getByName(String name) {
        return Arrays.stream(Timescale.values())
            .filter(t -> t.name.equals(name))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("No timescale exists for argument " + name));
    }

    public Function<OffsetDateTime, OffsetDateTime> getNext() {
        return next;
    }

    public Function<OffsetDateTime, OffsetDateTime> getGranularityFunction() {
        return granularityFunction;
    }

    private static int nanoToMilli(int nano) {
        int factor = 1_000;
        return (nano / factor) * 1_000;
    }
}
