package io.Adrestus.config;

public class ConsensusConfiguration {

    public static final int ERASURE_SERVER_PORT = 7082;
    public static final long CONSENSUS_COMMITTEE_TIMER = 2 * 1000;
    public static final long CONSENSUS_TIMER = 2 * 1000;
    public static final int CONSENSUS_TIMEOUT = 22 * 1000;
    public final static int HEARTBEAT_INTERVAL = 10;
    public final static int CYCLES = 1100;
    public final static int ERASURE_CYCLES = 1;
    public static final int CONSENSUS_CONNECTED_RECEIVE_TIMEOUT = 14 * 1000;
    public static final int CONSENSUS_CONNECTED_SEND_TIMEOUT = 22 * 1000;

    public static final int CONSENSUS_ERASURE_RECEIVE_TIMEOUT = 17000;
    public static final int CONSENSUS_ERASURE_SEND_TIMEOUT = 17000;
    public static final int CONSENSUS_COLLECTED_TIMEOUT = 14 * 1000;
    public static final int CONSENSUS_PUBLISHER_TIMEOUT = 4 * 1000;

    public static final long CHANGE_VIEW_TIMER = 2 * 1000;
    public static final int CHANGE_VIEW_COLLECTOR_TIMEOUT = 8 * 1000;
    public static final int CHANGE_VIEW_CONNECTED_TIMEOUT = 8 * 1000;
    public static final int CONSENSUS_TEST_TIMEOUT = 2 * 1000;
    public static final int CONSENSUS_WAIT_TIMEOUT = 2 * 1000;
    public static int CHANGE_VIEW_STATE_TRANSITION = 4;

    public static int EPOCH_TRANSITION = 15;

    public static final String CONSENSUS = "CONSENSUS";
    public static final String CHANGE_VIEW = "CHANGE_VIEW";


    public static final String CHUNKS_COLLECTOR_PORT = "5556";
    public static final String PUBLISHER_PORT = "5557";
    public static final String SUBSCRIBER_PORT = "5557";
    public static final String COLLECTOR_PORT = "5558";
    public static final String CONNECTED_PORT = "5559";

    public static final String HEARTBEAT_MESSAGE = "1";

}
