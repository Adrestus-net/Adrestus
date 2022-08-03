package io.Adrestus.util;

import com.sun.management.OperatingSystemMXBean;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.BlockingQueue;

public abstract class PoolSizeCalculator {
    private final int SAMPLE_QUEUE_SIZE = 1000;
    private final int EPSYLON = 20;
    private final OperatingSystemMXBean osBean;
    private volatile boolean expired;
    private final long testtime = 3000;

    protected Runnable creatTask() {
        return null;
    }

    protected BlockingQueue<Runnable> createWorkQueue() {
        return null;
    }

    protected long getCurrentThreadCPUTime() {
        ThreadMXBean tmxb = ManagementFactory.getThreadMXBean();
        return tmxb.getCurrentThreadCpuTime();
    }

    ;

    public PoolSizeCalculator() {
        osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
    }

    public int calculateOptimalThreadCount() {
        long cputime = getCurrentThreadCPUTime();
        long waittime = (testtime * 1000000) - cputime;


        BigDecimal waitTime = new BigDecimal(waittime);
        BigDecimal computeTime = new BigDecimal(cputime);
        BigDecimal numberOfCPU = new BigDecimal(Runtime.getRuntime().availableProcessors());
        BigDecimal optimalthreadcount = numberOfCPU.multiply(round(new BigDecimal(1).add(waitTime.divide(computeTime, RoundingMode.HALF_UP)), 0, true));
       /* System.out.println("Number of CPU: " + numberOfCPU);
        System.out.println("Target utilization: " + osBean.getProcessCpuLoad());
        System.out.println("Elapsed time (nanos): " + (testtime * 1000000));
        System.out.println("Compute time (nanos): " + cputime);
        System.out.println("Wait time (nanos): " + waittime);
        System.out.println("Formula: " + numberOfCPU + " * " + osBean.getProcessCpuLoad() + " * (1 + " + waitTime + " / " + computeTime + ")");
        System.out.println("* Optimal thread count: " + optimalthreadcount);*/
        return optimalthreadcount.intValue();
    }

    public long calculateMemoryUsage() {
        BlockingQueue<Runnable> queue = createWorkQueue();
        for (int i = 0; i < SAMPLE_QUEUE_SIZE; i++) {
            queue.add(creatTask());
        }
        long mem0 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long mem1 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        queue = null;
        collectGarbage(15);
        mem0 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        queue = createWorkQueue();
        for (int i = 0; i < SAMPLE_QUEUE_SIZE; i++) {
            queue.add(creatTask());
        }
        collectGarbage(15);
        mem1 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        return (mem1 - mem0) / SAMPLE_QUEUE_SIZE;
    }

    private void calculateOptimalCapacity(BigDecimal targetQueueSizeBytes) {
        long mem = calculateMemoryUsage();
        BigDecimal queueCapacity = targetQueueSizeBytes.divide(new BigDecimal(mem), RoundingMode.HALF_UP);
        /*System.out.println("Target queue memory usage (bytes): " + targetQueueSizeBytes);
        System.out.println("createTask() produced " + creatTask().getClass().getName() + " which took " + mem + " bytes in a queue");
        System.out.println("Formula: " + targetQueueSizeBytes + " / " + mem);
        System.out.println("* Recommended queue capacity (bytes): " + queueCapacity);*/
    }


    private void collectGarbage(int times) {
        for (int i = 0; i < times; i++) {
            System.gc();
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private BigDecimal round(BigDecimal d, int scale, boolean roundUp) {
        System.out.println("d" + d);
        int mode = (roundUp) ? BigDecimal.ROUND_UP : BigDecimal.ROUND_DOWN;
        return d.setScale(scale, BigDecimal.ROUND_UP);
    }
}
