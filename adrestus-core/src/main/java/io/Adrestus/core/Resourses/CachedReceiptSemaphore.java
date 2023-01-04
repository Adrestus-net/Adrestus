package io.Adrestus.core.Resourses;

import java.util.concurrent.Semaphore;

public class CachedReceiptSemaphore {
    private static volatile CachedReceiptSemaphore instance;
    private final Semaphore semaphore;

    private CachedReceiptSemaphore() {
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        }
        this.semaphore = new Semaphore(1);
    }

    public static CachedReceiptSemaphore getInstance() {
        var result = instance;
        if (result == null) {
            synchronized (CachedReceiptSemaphore.class) {
                result = instance;
                if (result == null) {
                    instance = result = new CachedReceiptSemaphore();
                }
            }
        }
        return result;
    }

    public static void setInstance(CachedReceiptSemaphore instance) {
        CachedReceiptSemaphore.instance = instance;
    }

    public Semaphore getSemaphore() {
        return semaphore;
    }
}
