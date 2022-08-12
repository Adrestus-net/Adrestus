package io.Adrestus.core.Resourses;

public class CachedLatestRandomness {
    private static volatile CachedLatestRandomness instance;
    private byte[] pRnd;
    private byte[] Rnd;

    private CachedLatestRandomness(byte[] pRnd, byte[] rnd) {
        this.pRnd = pRnd;
        Rnd = rnd;
    }

    private CachedLatestRandomness() {
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        }
    }

    public static CachedLatestRandomness getInstance() {
        var result = instance;
        if (result == null) {
            synchronized (CachedLatestRandomness.class) {
                result = instance;
                if (result == null) {
                    instance = result = new CachedLatestRandomness();
                }
            }
        }
        return result;
    }

    public byte[] getpRnd() {
        return pRnd;
    }

    public void setpRnd(byte[] pRnd) {
        this.pRnd = pRnd;
    }

    public byte[] getRnd() {
        return Rnd;
    }

    public void setRnd(byte[] rnd) {
        Rnd = rnd;
    }
}
