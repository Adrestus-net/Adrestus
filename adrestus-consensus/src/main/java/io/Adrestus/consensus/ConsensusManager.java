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
                this.role = new Supervisor();
                break;
            case VALIDATOR:
                this.role = new Validator();
                break;
            case ORGANIZER:
                this.role = new Organizer();
                break;
        }
    }
}
