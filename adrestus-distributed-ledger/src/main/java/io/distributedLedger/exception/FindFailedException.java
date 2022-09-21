package io.distributedLedger.exception;

public final class FindFailedException extends DatabaseIOException {

    public FindFailedException(final String message) {
        super(message);
    }

    public FindFailedException(
            final String message,
            final Throwable throwable
    ) {
        super(message, throwable);
    }
}
