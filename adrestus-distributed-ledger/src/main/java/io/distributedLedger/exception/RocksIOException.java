package io.distributedLedger.exception;

public abstract class RocksIOException extends Exception {

    public RocksIOException(final String message) {
        super(message);
    }

    public RocksIOException(
            final String message,
            final Throwable throwable
    ) {
        super(message, throwable);
    }
}

