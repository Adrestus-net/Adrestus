package io.distributedLedger.exception;

public final class SaveFailedException extends RocksIOException {

    public SaveFailedException(final String message) {
        super(message);
    }

    public SaveFailedException(
            final String message,
            final Throwable throwable
    ) {
        super(message, throwable);
    }
}
