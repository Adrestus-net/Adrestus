package io.Adrestus.core;

public enum StatusType {
    SUCCES("SUCCESS"),
    FAILED("FAILED"),
    PENDING("PENDING"),
    BUFFERED("BUFFERED"),
    ABORT("ABORT");
    private final String title;

    StatusType(String title) {
        this.title = title;
    }
}
