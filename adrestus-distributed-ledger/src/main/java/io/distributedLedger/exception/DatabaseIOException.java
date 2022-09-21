package io.distributedLedger.exception;

public abstract class DatabaseIOException extends Exception {

    public DatabaseIOException(final String message) {
        super(message);
    }

    public DatabaseIOException(
            final String message,
            final Throwable throwable
    ) {
        super(message, throwable);
    }
}

