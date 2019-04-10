package com.scottlogic.deg.generator.restrictions;

import java.util.Objects;
import java.util.Set;

public class SetRestrictions {
    private final Set<Object> whitelist;
    private final Set<Object> blacklist;

    public Set<Object> getWhitelist() {
        return this.whitelist;
    }

    public Set<Object> getBlacklist() {
        return this.blacklist;
    }

    public SetRestrictions(Set<Object> whitelist, Set<Object> blacklist) {
        this.whitelist = whitelist;
        this.blacklist = blacklist;
    }

    public boolean isEmpty(){
        return ((this.whitelist == null || this.whitelist.isEmpty())
            && (this.blacklist == null || this.blacklist.isEmpty()));
    }

    public static SetRestrictions fromWhitelist(Set<Object> whitelist) {
        return new SetRestrictions(whitelist, null);
    }

    public static SetRestrictions fromBlacklist(Set<Object> blacklist) {
        return new SetRestrictions(null, blacklist);
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
