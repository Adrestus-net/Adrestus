package io.Adrestus.core.Trie;
import java.util.stream.Stream;


public interface Trampoline<T> {
    T get();


    default Trampoline<T> jump() {
        return this;
    }


    default T result() {
        return get();
    }

    default boolean complete() {
        return true;
    }

    static <T> Trampoline<T> done(final T result) {
        return () -> result;
    }


    static <T> Trampoline<T> more(final Trampoline<Trampoline<T>> trampoline) {
        return new Trampoline<T>() {
            @Override
            public boolean complete() {
                return false;
            }

            @Override
            public Trampoline<T> jump() {
                return trampoline.result();
            }

            @Override
            public T get() {
                return trampoline(this);
            }

            T trampoline(final Trampoline<T> trampoline) {
                return Stream.iterate(trampoline, Trampoline::jump)
                        .filter(Trampoline::complete)
                        .findFirst()
                        .map(Trampoline::result)
                        .get();
            }
        };
    }
}
