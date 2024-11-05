package io.Adrestus.network;

import lombok.Getter;

import java.util.function.Supplier;

@Getter
public enum TopicType {

    ANNOUNCE_PHASE(AnnouncePhaseTopic::new),
    PREPARE_PHASE(PreparePhaseTopic::new),
    COMMITTEE_PHASE(CommitPhaseTopic::new),
    DISPERSE_PHASE1(DispersePhase1Topic::new),
    DISPERSE_PHASE2(DispersePhase2Topic::new);

    private final Supplier<ITopic> constructor;

    TopicType(Supplier<ITopic> constructor) {
        this.constructor = constructor;
    }
}
