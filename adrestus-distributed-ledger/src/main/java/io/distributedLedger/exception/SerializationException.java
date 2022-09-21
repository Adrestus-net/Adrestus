package io.distributedLedger.exception;

public final class SerializationException extends SerDeException {

    public SerializationException(final String message) {
        super(message);
    }

    public SerializationException(
            final String message,
            final Throwable throwable
    ) {
        super(message, throwable);
    }
}
