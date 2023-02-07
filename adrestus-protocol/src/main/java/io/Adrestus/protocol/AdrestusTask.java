package io.Adrestus.protocol;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class AdrestusTask {
    private static final AtomicInteger ID_GENERATOR = new AtomicInteger();
    private final int id;

    public AdrestusTask() {
        this.id = ID_GENERATOR.incrementAndGet();
    }

    public int getId() {
        return id;
    }


    public abstract void execute();

    public void close() {
    }


    @Override
    public String toString() {
        return String.format("id=%d timeMs=%d", id);
    }
}
