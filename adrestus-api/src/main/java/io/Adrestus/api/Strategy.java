package io.Adrestus.api;

public class Strategy {
    private IStrategy strategy;

    public Strategy(TransactionStrategy strategy) {
        this.strategy = strategy;
    }

    public void changeStrategy(TransactionStrategy strategy) {
        this.strategy = strategy;
    }

    public void SendTransactionSync() {
        strategy.execute();
    }

    public IStrategy getStrategy() {
        return strategy;
    }

    public void setStrategy(IStrategy strategy) {
        this.strategy = strategy;
    }

}
