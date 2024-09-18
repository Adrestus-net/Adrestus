package io.Adrestus.core.RewardMechanism;

public interface RewardHandler {
    boolean canHandleRequest(Request req);

    int getPriority();

    void handle(Request req);

    String name();
}
