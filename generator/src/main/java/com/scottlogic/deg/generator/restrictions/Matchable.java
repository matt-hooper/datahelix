package com.scottlogic.deg.generator.restrictions;

public interface Matchable<T> {
    boolean match(T object);
}
