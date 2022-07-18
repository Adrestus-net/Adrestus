package io.Adrestus.core.Trie;

import java.util.stream.Stream;


public interface RecursiveOptimizer<T> {
    T get();


    default RecursiveOptimizer<T> jump() {
        return this;
    }


    default T result() {
        return get();
    }

    default boolean complete() {
        return true;
    }

    static <T> RecursiveOptimizer<T> done(final T result) {
        return () -> result;
    }


    static <T> RecursiveOptimizer<T> more(final RecursiveOptimizer<RecursiveOptimizer<T>> trampoline) {
        return new RecursiveOptimizer<T>() {
            @Override
            public boolean complete() {
                return false;
            }

            @Override
            public RecursiveOptimizer<T> jump() {
                return trampoline.result();
            }

            @Override
            public T get() {
                return trampoline(this);
            }

            T trampoline(final RecursiveOptimizer<T> recursiveOptimizer) {
                return Stream.iterate(recursiveOptimizer, RecursiveOptimizer::jump)
                        .filter(RecursiveOptimizer::complete)
                        .findFirst()
                        .map(RecursiveOptimizer::result)
                        .get();
            }
        };
    }
}
