package io.Adrestus.core;

public enum TransactionStatus {
    SUCCES("SUCCES"),
    FAILED("FAILED"),
    PENDING("PENDING"),
    ABORT("ABORT");
    private final String title;

    TransactionStatus(String title) {
        this.title = title;
    }
}
