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

    public IStrategy getStrategy() {
        return strategy;
    }

    public void setStrategy(IStrategy strategy) {
        this.strategy = strategy;
    }

    public void block_until_send(){
        this.strategy.block_until_send();
    }
}
