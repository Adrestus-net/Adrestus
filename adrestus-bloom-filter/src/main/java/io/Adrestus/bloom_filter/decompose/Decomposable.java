package io.Adrestus.bloom_filter.decompose;

public interface Decomposable {

    /**
     * Decompose this object and render into the given {@link ByteSink} instance.
     *
     * @param into
     */
    public void decompose(ByteSink into);

}