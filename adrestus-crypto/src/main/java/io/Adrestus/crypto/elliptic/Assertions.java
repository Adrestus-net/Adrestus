package io.Adrestus.crypto.elliptic;

/** Assertion utility functions. */
public class Assertions {

    public static void verifyPrecondition(boolean assertionResult, String errorMessage) {
        if (!assertionResult) {
            throw new RuntimeException(errorMessage);
        }
    }
}
