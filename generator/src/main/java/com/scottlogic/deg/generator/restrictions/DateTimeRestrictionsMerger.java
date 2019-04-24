package com.scottlogic.deg.generator.restrictions;

import com.scottlogic.deg.generator.generation.fieldvaluesources.datetime.Timescale;
import com.sun.scenario.effect.Offset;

import java.time.OffsetDateTime;

public class DateTimeRestrictionsMerger {
    private enum MergeLimit {
        MIN, MAX
    }

    public MergeResult<DateTimeRestrictions> merge(DateTimeRestrictions left, DateTimeRestrictions right) {
        if (left == null && right == null)
            return new MergeResult<>(null);
        if (left == null)
            return new MergeResult<>(right);
        if (right == null)
            return new MergeResult<>(left);

        Timescale granularity = Timescale.getMostCoarse(left.getGranularity(), right.getGranularity());
        final DateTimeRestrictions merged = new DateTimeRestrictions(granularity);

        merged.min = granulate(MergeLimit.MIN, granularity, getMergedLimitStructure(MergeLimit.MIN, left.min, right.min));
        merged.max = granulate(MergeLimit.MAX, granularity, getMergedLimitStructure(MergeLimit.MAX, left.max, right.max));

        if (merged.min == null || merged.max == null) {
            return new MergeResult<>(merged);
        }

        if (merged.min.isAfter(merged.max)) {
            return MergeResult.UNSUCCESSFUL;
        }

        return new MergeResult<>(merged);
    }

    private DateTimeRestrictions.DateTimeLimit granulate(MergeLimit mergeLimit,
                                                         Timescale granularity,
                                                         DateTimeRestrictions.DateTimeLimit limitHolder) {
        if (limitHolder == null) {
            return limitHolder;
        }
        OffsetDateTime limit = limitHolder.getLimit();
        boolean inclusive = limitHolder.isInclusive();
        OffsetDateTime adjusted = granularity.getGranularityFunction().apply(limit);
        switch (mergeLimit) {
            case MIN:
                if (adjusted.equals(limit)) {
                    return new DateTimeRestrictions.DateTimeLimit(adjusted, inclusive);
                } else {
                    return new DateTimeRestrictions.DateTimeLimit(granularity.getNext().apply(adjusted), inclusive);
                }
            case MAX:
                return new DateTimeRestrictions.DateTimeLimit(adjusted, inclusive);


            default:
                throw new UnsupportedOperationException();
        }
    }

    private DateTimeRestrictions.DateTimeLimit getMergedLimitStructure(
        MergeLimit mergeLimit,
        DateTimeRestrictions.DateTimeLimit left,
        DateTimeRestrictions.DateTimeLimit right) {
        if (left == null && right == null) {
            return null;
        }
        if (left == null) {
            return right;
        }
        if (right == null) {
            return left;
        }

        if (left.getLimit().compareTo(right.getLimit()) == 0)
            return new DateTimeRestrictions.DateTimeLimit(left.getLimit(), left.isInclusive() && right.isInclusive());
        switch (mergeLimit) {
            case MIN:
                if (left.getLimit().compareTo(right.getLimit()) > 0)
                    return left;
                return right;
            case MAX:
                if (left.getLimit().compareTo(right.getLimit()) < 0)
                    return left;
                return right;
            default:
                throw new UnsupportedOperationException();
        }
    }
}
