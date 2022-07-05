package io.Adrestus.crypto.mnemonic;

public enum Security {
    NORMAL(128), HIGH(256);

    private int rawValue;

    Security(int rawValue) {
        this.rawValue = rawValue;
    }

    public int getRawValue() {
        return rawValue;
    }
}
