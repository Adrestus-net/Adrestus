package io.Adrestus.consensus;

import io.Adrestus.config.ConsensusConfiguration;
import lombok.SneakyThrows;

import java.util.Timer;
import java.util.TimerTask;

public class ConsensusTimer implements Runnable {

    private final Timer timer;
    private final ConsensusTask task;
    private volatile boolean exit;

    public ConsensusTimer() {
        this.timer = new Timer(ConsensusConfiguration.CONSENSUS);
        this.task = new ConsensusTask();
        this.exit = true;

    }


    public Timer getTimer() {
        return timer;
    }

    public ConsensusTask getTask() {
        return task;
    }

    public void stopThread() {
        this.exit = false;
    }

    @SneakyThrows
    @Override
    public void run() {
        this.timer.scheduleAtFixedRate(task, 0, ConsensusConfiguration.CONSENSUS_TIMER);
        while (exit) {
            Thread.sleep(500);
        }
    }

    protected final class ConsensusTask extends TimerTask {


        @Override
        public void run() {
            System.out.println("Executed");
        }

        @Override
        public boolean cancel() {
            super.cancel();
            return true;
        }
    }

}
