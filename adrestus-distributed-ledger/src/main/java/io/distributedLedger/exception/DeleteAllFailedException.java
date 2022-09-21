package io.distributedLedger.exception;

public final class DeleteAllFailedException extends DatabaseIOException {

    public DeleteAllFailedException(final String message) {
        super(message);
    }

    public DeleteAllFailedException(
            final String message,
            final Throwable throwable
    ) {
        super(message, throwable);
    }
}
