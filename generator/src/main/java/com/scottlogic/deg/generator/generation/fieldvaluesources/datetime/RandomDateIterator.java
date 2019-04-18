package com.scottlogic.deg.generator.generation.fieldvaluesources.datetime;

import com.scottlogic.deg.generator.utils.RandomNumberGenerator;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;

class RandomDateIterator implements Iterator<OffsetDateTime> {
    private final OffsetDateTime minDate;
    private final OffsetDateTime maxDate;
    private final RandomNumberGenerator random;
    private final ChronoUnit granularity;

    RandomDateIterator(OffsetDateTime minDate, OffsetDateTime maxDate, RandomNumberGenerator randomNumberGenerator, ChronoUnit granularity) {
        this.minDate = minDate;
        this.maxDate = maxDate;
        this.random = randomNumberGenerator;
        this.granularity = granularity;
    }

    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public OffsetDateTime next() {
        long min = getMilli(minDate);
        long max = getMilli(maxDate) - 1;

        long generatedLong = (long) random.nextDouble(min, max);

        OffsetDateTime generatedDate = Instant.ofEpochMilli(generatedLong).atZone(ZoneOffset.UTC).toOffsetDateTime();

        return trimUnwantedGranularity(generatedDate, granularity);
    }

    private long getMilli(OffsetDateTime date) {
        return date.toInstant().toEpochMilli();
    }

    private int nanoToMilli(int nano) {
        int factor = 1_000;
        return (nano / factor) * 1_000;
    }

    private OffsetDateTime trimUnwantedGranularity(OffsetDateTime dateToTrim, ChronoUnit granularity) {
        switch (granularity) {
            case MILLIS:
                return OffsetDateTime.of(dateToTrim.getYear(), dateToTrim.getMonth().getValue(), dateToTrim.getDayOfMonth(), dateToTrim.getHour(), dateToTrim.getMinute(), dateToTrim.getSecond(), nanoToMilli(dateToTrim.getNano()), ZoneOffset.UTC);
            case SECONDS:
                return OffsetDateTime.of(dateToTrim.getYear(), dateToTrim.getMonth().getValue(), dateToTrim.getDayOfMonth(), dateToTrim.getHour(), dateToTrim.getMinute(), dateToTrim.getSecond(), 0, ZoneOffset.UTC);

            case MINUTES:
                return OffsetDateTime.of(dateToTrim.getYear(), dateToTrim.getMonth().getValue(), dateToTrim.getDayOfMonth(), dateToTrim.getHour(), dateToTrim.getMinute(), 0, 0, ZoneOffset.UTC);

            case HOURS:
                return OffsetDateTime.of(dateToTrim.getYear(), dateToTrim.getMonth().getValue(), dateToTrim.getDayOfMonth(), dateToTrim.getHour(), 0, 0, 0, ZoneOffset.UTC);

            case DAYS:
                return OffsetDateTime.of(dateToTrim.getYear(), dateToTrim.getMonth().getValue(), dateToTrim.getDayOfMonth(), 0, 0, 0, 0, ZoneOffset.UTC);

            case MONTHS:
                return OffsetDateTime.of(dateToTrim.getYear(), dateToTrim.getMonth().getValue(), 1, 0, 0, 0, 0, ZoneOffset.UTC);

            case YEARS:
                return OffsetDateTime.of(dateToTrim.getYear(), 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

            default:
                throw new IllegalStateException("Unimplemented granularity specified.");

        }
    }
}
