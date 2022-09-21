package io.distributedLedger.exception;

public final class DeleteFailedException extends DatabaseIOException {

    public DeleteFailedException(final String message) {
        super(message);
    }

    public DeleteFailedException(
            final String message,
            final Throwable throwable
    ) {
        super(message, throwable);
    }
}
