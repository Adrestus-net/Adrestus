package io.Adrestus.protocol;

import com.google.common.base.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Worker implements Runnable {
    private static Logger LOGGER = LoggerFactory.getLogger(Worker.class);
    private final AdrestusTask task;

    public Worker(final AdrestusTask task) {
        this.task = task;
    }

    @Override
    public void run() {
        task.execute();
    }

    public AdrestusTask getTask() {
        return task;
    }

    public void close() {
        task.close();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Worker worker = (Worker) o;
        return Objects.equal(task, worker.task);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(task);
    }

    @Override
    public String toString() {
        return "Worker{" +
                "task=" + task +
                '}';
    }
}
