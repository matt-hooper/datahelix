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
        long min = this.minDate.toInstant().toEpochMilli();
        long max = this.maxDate.toInstant().toEpochMilli() - 1;

        long generatedLong = (long) random.nextDouble(min, max);

        return trimUnwantedGranularity(Instant.ofEpochMilli(generatedLong).atZone(ZoneOffset.UTC).toOffsetDateTime(), granularity);
    }

    private int nanoToMilli(int nano) {
        int factor = 1_000;
        return (nano / factor) * 1_000;
    }

    private OffsetDateTime trimUnwantedGranularity(OffsetDateTime dateToTrim, ChronoUnit granularity) {
        return OffsetDateTime.of(trimUnwantedGranularity(dateToTrim.toLocalDateTime(), granularity), ZoneOffset.UTC);
    }

    private LocalDateTime trimUnwantedGranularity(LocalDateTime dateToTrim, ChronoUnit granularity) {
        switch (granularity) {
            case MILLIS:
                return LocalDateTime.of(dateToTrim.getYear(), dateToTrim.getMonth(), dateToTrim.getDayOfMonth(), dateToTrim.getHour(), dateToTrim.getMinute(), dateToTrim.getSecond(), nanoToMilli(dateToTrim.getNano()));
            case SECONDS:
                return LocalDateTime.of(dateToTrim.getYear(), dateToTrim.getMonth(), dateToTrim.getDayOfMonth(), dateToTrim.getHour(), dateToTrim.getMinute(), dateToTrim.getSecond(), 0);

            case MINUTES:
                return LocalDateTime.of(dateToTrim.getYear(), dateToTrim.getMonth(), dateToTrim.getDayOfMonth(), dateToTrim.getHour(), dateToTrim.getMinute());

            case HOURS:
                return LocalDateTime.of(dateToTrim.getYear(), dateToTrim.getMonth(), dateToTrim.getDayOfMonth(), dateToTrim.getHour(), 0);

            case DAYS:
                return LocalDateTime.of(dateToTrim.getYear(), dateToTrim.getMonth(), dateToTrim.getDayOfMonth(), 0, 0);

            case MONTHS:
                return LocalDateTime.of(dateToTrim.getYear(), dateToTrim.getMonth(), 1, 0, 0, 0, 0);

            case YEARS:
                return LocalDateTime.of(dateToTrim.getYear(), Month.values()[1], 1, 0, 0, 0, 0);
            //LocalDateTime of(int year, Month month, int dayOfMonth, int hour, int minute, int second, int nanoOfSecond)

            default:
                throw new IllegalStateException("Unimplemented granularity specified.");

        }
    }
}
