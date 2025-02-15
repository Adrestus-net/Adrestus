package io.Adrestus.consensus;

public class ConsensusManager {
    private ConsensusRole role;

    public ConsensusManager(ConsensusRole role) {
        this.role = role;
    }

    public ConsensusManager() {
    }

    public ConsensusRole getRole() {
        return this.role;
    }

    public void changeStateTo(ConsensusRoleType type) {
        switch (type) {
            case SUPERVISOR:
                this.role = Supervisor.getInstance();
                break;
            case VALIDATOR:
                this.role = Validator.getInstance();
                break;
            case ORGANIZER:
                this.role = Organizer.getInstance();
                break;
        }
    }

    public void clearStateTo(ConsensusRoleType type, boolean DEBUG) {
        switch (type) {
            case SUPERVISOR:
                Supervisor.getInstance().clear(DEBUG);
                break;
            case VALIDATOR:
                Validator.getInstance().clear(DEBUG);
                break;
            case ORGANIZER:
                Organizer.getInstance().clear(DEBUG);
                break;
        }
    }
}
