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
                this.role = Supervisor.getInstance(this.DEBUG);
                break;
            case VALIDATOR:
                this.role = Validator.getInstance(this.DEBUG);
                break;
            case ORGANIZER:
                this.role = Organizer.getInstance(this.DEBUG);
                break;
        }
    }

    public void clearStateTo(ConsensusRoleType type, boolean DEBUG) {
        switch (type) {
            case SUPERVISOR:
                Supervisor.getInstance(this.DEBUG).clear(DEBUG);
                break;
            case VALIDATOR:
                Validator.getInstance(this.DEBUG).clear(DEBUG);
                break;
            case ORGANIZER:
                Organizer.getInstance(this.DEBUG).clear(DEBUG);
                break;
        }
    }
}
