package io.Adrestus.protocol;

import io.Adrestus.core.Resourses.MemoryTransactionPool;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WorkerTest {

    @Test
    public void test() throws InterruptedException {
        IAdrestusFactory factory = new AdrestusFactory();
        List<AdrestusTask> tasks = new java.util.ArrayList<>(List.of(
                factory.createTransactionTask(),
                factory.createBindServerReceiptTask(),
                factory.createSendReceiptTask(),
                factory.createRepositoryTransactionTask(),
                factory.createRepositoryCommitteeTask()));
        ExecutorService executor = Executors.newFixedThreadPool(tasks.size());
        tasks.stream().map(Worker::new).forEach(executor::execute);
        // All tasks were executed, now shutdown
        Thread.sleep(2000);
        tasks.forEach(val -> {
            val.close();
        });
        tasks.clear();
        executor.shutdown();
        while (!executor.isTerminated()) {
            Thread.yield();
        }

    }

    @Test
    public void test2() throws Exception {
        IAdrestusFactory factory = new AdrestusFactory();
        List<AdrestusTask> tasks = new java.util.ArrayList<>(List.of(
                factory.createTransactionTask(),
                factory.createBindServerReceiptTask(),
                factory.createSendReceiptTask(),
                factory.createRepositoryTransactionTask(),
                factory.createRepositoryCommitteeTask()));
        ExecutorService executor = Executors.newFixedThreadPool(tasks.size());
        tasks.stream().map(Worker::new).forEach(executor::execute);
        // All tasks were executed, now shutdown
        int size = 0;
        while (true) {
            Thread.sleep(2000);
            int left = MemoryTransactionPool.getInstance().getAll().size() - size;
            System.out.println(left);
            size = size + MemoryTransactionPool.getInstance().getAll().size();
        }

    }
}
