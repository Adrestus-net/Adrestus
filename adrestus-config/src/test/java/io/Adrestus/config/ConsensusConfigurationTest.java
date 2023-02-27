package io.Adrestus.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConsensusConfigurationTest {
    @Test
    public void test() {
        assertEquals(2 * 1000, ConsensusConfiguration.CONSENSUS_COMMITTEE_TIMER);
        assertEquals(2 * 1000, ConsensusConfiguration.CONSENSUS_TIMER);
        assertEquals(22 * 1000, ConsensusConfiguration.CONSENSUS_TIMEOUT);
        assertEquals(8 * 1000, ConsensusConfiguration.CONSENSUS_CONNECTED_RECEIVE_TIMEOUT);
        assertEquals(4 * 1000, ConsensusConfiguration.CONSENSUS_COLLECTED_TIMEOUT);
        assertEquals(4 * 1000, ConsensusConfiguration.CONSENSUS_PUBLISHER_TIMEOUT);


        assertEquals(2 * 1000, ConsensusConfiguration.CHANGE_VIEW_TIMER);
        assertEquals(8 * 1000, ConsensusConfiguration.CHANGE_VIEW_COLLECTOR_TIMEOUT);
        assertEquals(8 * 1000, ConsensusConfiguration.CHANGE_VIEW_CONNECTED_TIMEOUT);
        assertEquals(2 * 1000, ConsensusConfiguration.CONSENSUS_TEST_TIMEOUT);
        assertEquals(15, ConsensusConfiguration.EPOCH_TRANSITION);
        assertEquals("CONSENSUS", ConsensusConfiguration.CONSENSUS);
        assertEquals("CHANGE_VIEW", ConsensusConfiguration.CHANGE_VIEW);

        assertEquals("5557", ConsensusConfiguration.PUBLISHER_PORT);
        assertEquals("5557", ConsensusConfiguration.SUBSCRIBER_PORT);
        assertEquals("5558", ConsensusConfiguration.COLLECTOR_PORT);
        assertEquals("5559", ConsensusConfiguration.CONNECTED_PORT);
        assertEquals("1", ConsensusConfiguration.HEARTBEAT_MESSAGE);

    }
}
