package com.scottlogic.deg.generator.restrictions;

import com.scottlogic.deg.generator.utils.SetUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * For a given combination of choices over the decision tree
 * Details every column's atomic constraints
 */
public class SetRestrictionsMerger {
    private static final SetRestrictions neutral = new SetRestrictions(null, new HashSet<>());

    public static MergeResult<SetRestrictions> merge(SetRestrictions a, SetRestrictions b) {
        if (a == null && b == null)
            return new MergeResult<>(null);

        a = coalesce(a, neutral);
        b = coalesce(b, neutral);

        Set<Object> newWhitelist;
        if (a.getWhitelist() == null && b.getWhitelist() == null)
            newWhitelist = null;
        else if (a.getWhitelist() == null)
            newWhitelist = b.getWhitelist();
        else if (b.getWhitelist() == null)
            newWhitelist = a.getWhitelist();
        else
            newWhitelist = SetUtils.intersect(a.getWhitelist(), b.getWhitelist());

        Set<Object> newBlacklist = SetUtils.union(
            coalesce(a.getBlacklist(), Collections.emptySet()),
            coalesce(b.getBlacklist(), Collections.emptySet()));

        if (newWhitelist != null && newBlacklist != null) {
            Set<Object> whiteAndBlacklistIntersection = SetUtils.intersect(
                newBlacklist,
                newWhitelist);

            if (whiteAndBlacklistIntersection.size() > 0) {
                newWhitelist = newWhitelist.stream()
                    .filter(val -> !whiteAndBlacklistIntersection.contains(val))
                    .collect(Collectors.toSet());
            }

            newBlacklist = Collections.emptySet();
        }

        if (newWhitelist != null && newWhitelist.size() == 0) {
            return new MergeResult<>();
        }

        return new MergeResult<>(new SetRestrictions(newWhitelist, newBlacklist));
    }

    private static <T> T coalesce(T preferred, T fallback) {
        return preferred != null ? preferred : fallback;
    }

}
