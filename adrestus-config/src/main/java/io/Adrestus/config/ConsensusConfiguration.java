package io.Adrestus.config;

public class ConsensusConfiguration {

    public static final long CONSENSUS_COMMITTEE_TIMER = 2 * 1000;
    public static final long CONSENSUS_TIMER = 2 * 1000;
    public static final long CHANGE_VIEW_TIMER = 2 * 1000;
    public static final int CONSENSUS_TIMEOUT = 22 * 1000;
    public static final int CHANGE_VIEW_COLLECTOR_TIMEOUT = 8 * 1000;
    public static final int CHANGE_VIEW_CONNECTED_TIMEOUT = 8 * 1000;
    public static final int CONSENSUS_TEST_TIMEOUT = 2 * 1000;
    public static final int EPOCH_TRANSITION = 4;
    public static final String CONSENSUS = "CONSENSUS";
    public static final String CHANGE_VIEW = "CHANGE_VIEW";


    public static final String PUBLISHER_PORT = "5557";
    public static final String SUBSCRIBER_PORT = "5557";
    public static final String COLLECTOR_PORT = "5558";
    public static final String CONNECTED_PORT = "5559";

    public static final String HEARTBEAT_MESSAGE = "1";

}
