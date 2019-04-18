package com.scottlogic.deg.generator.restrictions;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import static com.scottlogic.deg.generator.utils.NumberUtils.coerceToBigDecimal;

public class DateTimeRestrictions {
    private static final ChronoUnit DEFAULT_GRANULARITY = ChronoUnit.MILLIS;
    private final ChronoUnit granularity;
    public DateTimeLimit min;
    public DateTimeLimit max;

    public DateTimeRestrictions() {
        this(DEFAULT_GRANULARITY);
    }

    public DateTimeRestrictions(final ChronoUnit granularity) {
        this.granularity = granularity;
    }

    public ChronoUnit getGranularity() {
        return granularity;
    }

    public static boolean isDateTime(Object o) {
        return o instanceof OffsetDateTime;
    }

    public boolean match(Object o) {
        if (!DateTimeRestrictions.isDateTime(o)) {
            return false;
        }

        OffsetDateTime d = (OffsetDateTime) o;

        if (min != null) {
            if (d.compareTo(min.getLimit()) < (min.isInclusive() ? 0 : 1)) {
                return false;
            }
        }

        if (max != null) {
            if (d.compareTo(max.getLimit()) > (max.isInclusive() ? 0 : -1)) {
                return false;
            }
        }

        return isCorrectGranularity(d);
    }

    private boolean isCorrectGranularity(OffsetDateTime inputDate) {
        long epochMillis = getEpochMillis(inputDate);
        if (granularity == ChronoUnit.MONTHS || granularity == ChronoUnit.YEARS) {
            // TODO: compare based on months or years
            throw new RuntimeException("IMPLEMENT ME!");
        } else {
            long granularityMillis = granularity.getDuration().toMillis();
            return epochMillis % granularityMillis == 0;
        }
    }

    private long getEpochMillis(OffsetDateTime date) {
        return date.toInstant().toEpochMilli();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DateTimeRestrictions that = (DateTimeRestrictions) o;
        return Objects.equals(min, that.min) &&
            Objects.equals(max, that.max);
    }

    @Override
    public int hashCode() {
        return Objects.hash(min, max);
    }

    public static class DateTimeLimit {
        private final OffsetDateTime limit;
        private final boolean inclusive;

        public DateTimeLimit(OffsetDateTime limit, boolean inclusive) {
            this.limit = limit;
            this.inclusive = inclusive;
        }

        public OffsetDateTime getLimit() {
            return limit;
        }

        public boolean isInclusive() {
            return inclusive;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DateTimeLimit that = (DateTimeLimit) o;
            return inclusive == that.inclusive &&
                Objects.equals(limit, that.limit);
        }

        @Override
        public int hashCode() {
            return Objects.hash(limit, inclusive);
        }

        public boolean isAfter(DateTimeLimit max) {
            OffsetDateTime minLimit = getReferenceTime(1);
            OffsetDateTime maxLimit = max.getReferenceTime(-1);

            return minLimit.isAfter(maxLimit);
        }

        private OffsetDateTime getReferenceTime(int nanoOffset) {
            if (inclusive) {
                return limit;
            }

            return limit.plusNanos(nanoOffset);
        }
    }
}
