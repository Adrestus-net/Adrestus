package io.Adrestus.util;

public class RLPException extends RuntimeException {
    public RLPException(final String message) {
        this(message, null);
    }

    RLPException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}
