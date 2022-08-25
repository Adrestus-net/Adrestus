package io.Adrestus.consensus;

public class ConsensusManager {
    private ConsensusRole role;
    private boolean DEBUG;

    public ConsensusManager(ConsensusRole role, boolean DEBUG) {
        this.role = role;
        this.DEBUG = DEBUG;
    }

    public ConsensusManager(boolean DEBUG) {
        this.DEBUG = DEBUG;
    }

    public ConsensusRole getRole() {
        return this.role;
    }

    public void changeStateTo(ConsensusRoleType type) {
        switch (type) {
            case SUPERVISOR:
                this.role = new Supervisor();
                break;
            case VALIDATOR:
                this.role = new Validator(this.DEBUG);
                break;
            case ORGANIZER:
                this.role = new Organizer(this.DEBUG);
                break;
        }
    }
}
