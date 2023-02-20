package io.Adrestus.consensus;

public class ConsensusDataState {
    protected static AbstractState committee_state;

    protected static AbstractState previous_state;

    public static AbstractState getCommittee_state() {
        return committee_state;
    }

    public static void setCommittee_state(AbstractState committee_state) {
        ConsensusDataState.committee_state = committee_state;
    }

    public static AbstractState getPrevious_state() {
        return previous_state;
    }

    public static void setPrevious_state(AbstractState previous_state) {
        ConsensusDataState.previous_state = previous_state;
    }


    @Override
    public String toString() {
        return "ConsensusDataState{}";
    }
}
