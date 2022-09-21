package io.distributedLedger.exception;

public class EmptyFailedException extends DatabaseIOException {

    public EmptyFailedException(final String message) {
        super(message);
    }

    public EmptyFailedException(
            final String message,
            final Throwable throwable
    ) {
        super(message, throwable);
    }
}
