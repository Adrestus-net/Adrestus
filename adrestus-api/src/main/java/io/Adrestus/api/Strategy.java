package io.Adrestus.api;

public class Strategy {
    private IStrategy strategy;

    public Strategy(TransactionStrategy strategy) {
        this.strategy = strategy;
    }

    public void changeStrategy(TransactionStrategy strategy) {
        this.strategy = strategy;
    }

    public void execute() {
        strategy.execute();
    }
    public void terminate(){
        strategy.terminate();
    }
}
