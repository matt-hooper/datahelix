package com.scottlogic.deg.generator.utils;

import java.util.*;

public class ConcatenatingIterable<T> implements Iterable<T> {
    private final List<Iterable<T>> underlyingIterables;

    public ConcatenatingIterable(List<Iterable<T>> underlyingIterables) {
        this.underlyingIterables = underlyingIterables;
    }

    public ConcatenatingIterable(Iterable<T>... underlyingIterables) {
        this.underlyingIterables = Arrays.asList(underlyingIterables);
    }

    @Override
    public Iterator<T> iterator() {
        Queue<Iterator<T>> iteratorQueue = new LinkedList<>();

        underlyingIterables.stream()
            .map(Iterable::iterator)
            .forEach(iteratorQueue::add);

        return new ConcatenatingIterable.InternalIterator(iteratorQueue);
    }

    private class InternalIterator implements Iterator<T>
    {
        private final Queue<Iterator<T>> underlyingIterators;

        private InternalIterator(Queue<Iterator<T>> underlyingIterators) {
            this.underlyingIterators = underlyingIterators;
        }

        @Override
        public boolean hasNext() {
            while (!this.underlyingIterators.isEmpty())
            {
                if (this.underlyingIterators.peek().hasNext())
                    return true;

                this.underlyingIterators.remove();
            }

            return false;
        }

        @Override
        public T next() {
            while (true) {
                Iterator<T> currentIterator = this.underlyingIterators.peek();

                if (currentIterator.hasNext())
                    return currentIterator.next();

                this.underlyingIterators.remove();
            }
        }
    }
}
