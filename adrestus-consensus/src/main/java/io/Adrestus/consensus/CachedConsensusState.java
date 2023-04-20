package io.Adrestus.consensus;

import com.google.common.base.Objects;
import io.Adrestus.consensus.ChangeView.ChangeViewCommitteeState;
import io.Adrestus.consensus.ChangeView.ChangeViewTransactionState;

public class CachedConsensusState {
    private static volatile CachedConsensusState instance;
    private AbstractState committeeState;
    private AbstractState transactionState;

    private CachedConsensusState() {
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        }
    }


    public static CachedConsensusState getInstance() {

        var result = instance;
        if (result == null) {
            synchronized (CachedConsensusState.class) {
                result = instance;
                if (result == null) {
                    instance = result = new CachedConsensusState();
                }
            }
        }
        return result;
    }

    public AbstractState getCommitteeState() {
        return committeeState;
    }

    public void setCommitteeState(AbstractState committeeState) {
        this.committeeState = committeeState;
    }

    public AbstractState getTransactionState() {
        return transactionState;
    }

    public void setTransactionState(AbstractState transactionState) {
        this.transactionState = transactionState;
    }

    public boolean isValid() {
        if (transactionState == null || committeeState == null)
            return true;
        return (this.committeeState.getClass().equals(ChangeViewCommitteeState.class) || this.transactionState.getClass().equals(ChangeViewTransactionState.class)) ? false : true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CachedConsensusState that = (CachedConsensusState) o;
        return Objects.equal(committeeState, that.committeeState) && Objects.equal(transactionState, that.transactionState);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(committeeState, transactionState);
    }

    @Override
    public String toString() {
        return "CachedConsensusState{" +
                "committeeState=" + committeeState +
                ", transactionState=" + transactionState +
                '}';
    }
}
