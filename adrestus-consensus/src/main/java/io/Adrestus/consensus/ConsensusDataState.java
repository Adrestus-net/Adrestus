package io.Adrestus.consensus;

public class ConsensusDataState {
    protected static AbstractState committee_state;

    public static AbstractState getCommittee_state() {
        return committee_state;
    }

    public static void setCommittee_state(AbstractState committee_state) {
        ConsensusDataState.committee_state = committee_state;
    }
}
