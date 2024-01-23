package io.Adrestus.bloom_filter.decompose;

import java.nio.charset.Charset;

public class DefaultDecomposer implements Decomposer<Object> {

    /**
     * The default platform encoding
     */
    private static final Charset DEFAULT_CHARSET = Charset.defaultCharset();

    /**
     * Decompose the object
     */
    @Override
    public void decompose(Object object, ByteSink sink) {
        if (object == null) {
            return;
        }

        if (object instanceof String) {
            sink.putBytes(((String) object).getBytes(DEFAULT_CHARSET));
            return;
        }

        sink.putBytes(object.toString().getBytes(DEFAULT_CHARSET));
    }

}