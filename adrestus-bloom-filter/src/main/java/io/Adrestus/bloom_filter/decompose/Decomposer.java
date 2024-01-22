package io.Adrestus.bloom_filter.decompose;

public interface Decomposer<T> {

	/**
	 * Decompose the object into the given {@link ByteSink}
	 * 
	 * @param object
	 *            the object to be decomposed
	 * 
	 * @param sink
	 *            the sink to which the object is decomposed
	 */
	public void decompose(T object, ByteSink sink);

}