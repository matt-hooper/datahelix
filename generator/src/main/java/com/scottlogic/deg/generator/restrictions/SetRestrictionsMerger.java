package com.scottlogic.deg.generator.restrictions;

import com.google.common.collect.Sets;

import java.util.HashSet;
import java.util.Set;

/**
 * For a given combination of choices over the decision tree
 * Details every column's atomic constraints
 */
public class SetRestrictionsMerger {
    public static MergeResult<SetRestrictions> merge(SetRestrictions a, SetRestrictions b) {
        if (a == null && b == null)
            return new MergeResult<>(null);
        if (a == null)
            return new MergeResult<>(b);
        if (b == null)
            return new MergeResult<>(a);

        if (a.hasWhitelist() && b.hasWhitelist()) {
            return mergeWhitelists(a.getWhitelist(), b.getWhitelist());
        }

        if (a.hasBlacklist() && b.hasBlacklist()){
            return mergeBlacklists(a.getBlacklist(), b.getBlacklist());
        }

        if (a.hasWhitelist()){
            return mergeWhitelistWithBlacklist(a.getWhitelist(), b.getBlacklist());
        }

        return mergeWhitelistWithBlacklist(b.getWhitelist(), a.getBlacklist());
    }

    private static MergeResult<SetRestrictions> mergeWhitelists(Set<Object> whitelistA, Set<Object> whitelistB) {
        Set<Object> union;
        if (whitelistA == null){
            union = whitelistB;
        }
        else if (whitelistB == null){
            union = whitelistA;
        }
        else {
            union = Sets.intersection(whitelistA, whitelistB);
        }

        if (union != null && union.isEmpty()){
            return new MergeResult<>();
        }
        return new MergeResult<>(SetRestrictions.fromWhitelist(union));
    }

    private static MergeResult<SetRestrictions> mergeBlacklists(Set<Object> blacklistA, Set<Object> blacklistB) {
        Set<Object> newBlacklist = new HashSet<>(blacklistA);
        newBlacklist.addAll(blacklistB);

        return new MergeResult<>(SetRestrictions.fromBlacklist(newBlacklist));
    }

    private static MergeResult<SetRestrictions> mergeWhitelistWithBlacklist(Set<Object> whitelist, Set<Object> blacklist) {
        if (whitelist == null){
            return new MergeResult<>(SetRestrictions.fromBlacklist(blacklist));
        }
        Set<Object> newWhitelist = new HashSet<>(whitelist);
        newWhitelist.removeAll(blacklist);

        if (whitelist.isEmpty()){
            return new MergeResult<>();
        }

        return new MergeResult<>(SetRestrictions.fromWhitelist(newWhitelist));
    }
}
