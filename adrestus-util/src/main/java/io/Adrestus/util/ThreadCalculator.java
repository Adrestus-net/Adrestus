package io.Adrestus.util;


public class ThreadCalculator extends PoolSizeCalculator {

    private int threadcount;

    public ThreadCalculator(int threadcount) {
        super();
        this.threadcount = threadcount;
    }

    public ThreadCalculator() {
        super();
    }

    public int getThreadcount() {
        return threadcount;
    }

    public void setThreadcount(int threadcount) {
        this.threadcount = threadcount;
    }

    @Override
    public String toString() {
        return "ThreadCalculator{" +
                "threadcount=" + threadcount +
                '}';
    }
}
