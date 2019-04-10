package com.scottlogic.deg.generator.restrictions;

import java.util.Objects;
import java.util.Set;

public class SetRestrictions {
    private final Set<Object> whitelist;
    private final Set<Object> blacklist;

    public static SetRestrictions fromWhitelist(Set<Object> whitelist) {
        return new SetRestrictions(whitelist, null);
    }

    public static SetRestrictions fromBlacklist(Set<Object> blacklist) {
        return new SetRestrictions(null, blacklist);
    }

    public Set<Object> getWhitelist() {
        if (hasBlacklist()){
            throw new UnsupportedOperationException("can't get a blacklist for a whitelist");
        }
        return whitelist;
    }

    public Set<Object> getBlacklist() {
        if (hasWhitelist()){
            throw new UnsupportedOperationException("can't get a whitelist for a blacklist");
        }
        return blacklist;
    }

    public boolean hasWhitelist() {
        return blacklist == null;
    }

    public boolean hasBlacklist() {
        return !hasWhitelist();
    }

    protected SetRestrictions(Set<Object> whitelist, Set<Object> blacklist) {
        this.whitelist = whitelist;
        this.blacklist = blacklist;
    }

    @Override
    public String toString() {
        if (isEmpty())
            return "<empty>";

        if (whitelist == null || whitelist.isEmpty())
            return String.format(
                    "NOT IN %s",
                    Objects.toString(blacklist));

        if (blacklist == null || blacklist.isEmpty())
            return String.format(
                    "IN %s",
                    Objects.toString(whitelist));

        return String.format(
            "IN %s AND NOT IN %s",
            Objects.toString(whitelist, "-"),
            Objects.toString(blacklist, "-"));
    }

    private boolean isEmpty(){
        return ((whitelist == null || whitelist.isEmpty())
            && (blacklist == null || blacklist.isEmpty()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SetRestrictions that = (SetRestrictions) o;
        return Objects.equals(whitelist, that.whitelist) &&
            Objects.equals(blacklist, that.blacklist);
    }

    @Override
    public int hashCode() {
        return Objects.hash(whitelist, blacklist);
    }
}
