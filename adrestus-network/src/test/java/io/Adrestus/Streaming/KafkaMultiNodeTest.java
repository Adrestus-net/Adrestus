package io.Adrestus.Streaming;

import io.Adrestus.network.ConsensusBroker;
import io.Adrestus.streaming.TopicType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.IntStream;

import static junit.framework.Assert.assertEquals;

public class KafkaMultiNodeTest {

    private static final String ANNOUNCE_MESSAGE_VALIDATORS = "ANNOUNCE_MESSAGE_FROM_VALIDATORS";
    private static final String PREPARE_MESSAGE_VALIDATORS = "PREPARE_MESSAGE_FROM_VALIDATORS";
    private static final String COMMITTEE_MESSAGE_VALIDATORS = "COMMITTEE_MESSAGE_FROM_VALIDATORS";

    private static final String ANNOUNCE_MESSAGE_LEADER = "ANNOUNCE_MESSAGE_FROM_LEADER";
    private static final String PREPARE_MESSAGE_LEADER = "PREPARE_MESSAGE_FROM_LEADER";
    private static final String COMMITTEE_MESSAGE_LEADER = "COMMITTEE_MESSAGE_FROM_LEADER";
    private static final int VIEW_NUMBER = 1;

    private static ArrayList<String> list;
    private static ConsensusBroker consensusBroker;
    private static OptionalInt position;

    @BeforeAll
    public static void setup() throws IOException {
        if (System.getenv("MAVEN_OPTS") != null) {
            System.out.println("Running from Maven: ");
            return;
        }
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress("google.com", 80));
        String IP = socket.getLocalAddress().getHostAddress();
        list = new ArrayList<>();
        list.add("192.168.1.106");
        list.add("192.168.1.116");
        list.add("192.168.1.115");
        position = IntStream.range(0, list.size()).filter(i -> IP.equals(list.get(i))).findFirst();
        consensusBroker = new ConsensusBroker(list, list.get(position.getAsInt()), position.getAsInt());
    }

    @Test
    public void testKafkaConsensusThreePhases() {
        consensusBroker.initializeKafkaKingdom();
        if (position.getAsInt() == 0) {
            consensusBroker.produceMessage(TopicType.ANNOUNCE_PHASE, String.valueOf(VIEW_NUMBER), ANNOUNCE_MESSAGE_LEADER);
            List<String> res=consensusBroker.receiveMessageFromValidators(TopicType.ANNOUNCE_PHASE, String.valueOf(VIEW_NUMBER));
            res.forEach(val -> assertEquals(ANNOUNCE_MESSAGE_VALIDATORS, val));
            consensusBroker.produceMessage(TopicType.PREPARE_PHASE, String.valueOf(VIEW_NUMBER), PREPARE_MESSAGE_LEADER);
            List<String> res1=consensusBroker.receiveMessageFromValidators(TopicType.PREPARE_PHASE, String.valueOf(VIEW_NUMBER));
            res1.forEach(val -> assertEquals(PREPARE_MESSAGE_VALIDATORS, val));
            consensusBroker.produceMessage(TopicType.COMMITTEE_PHASE, String.valueOf(VIEW_NUMBER), COMMITTEE_MESSAGE_LEADER);
            List<String> res3=consensusBroker.receiveMessageFromValidators(TopicType.PREPARE_PHASE, String.valueOf(VIEW_NUMBER));
            res3.forEach(val -> assertEquals(COMMITTEE_MESSAGE_VALIDATORS, val));
        } else {
            Optional<String> message = consensusBroker.receiveMessageFromLeader(TopicType.ANNOUNCE_PHASE, String.valueOf(VIEW_NUMBER), list.get(0));
            assert (message.isPresent());
            assertEquals(ANNOUNCE_MESSAGE_LEADER, message.get());
            consensusBroker.produceMessage(TopicType.ANNOUNCE_PHASE, String.valueOf(VIEW_NUMBER), ANNOUNCE_MESSAGE_LEADER);

            Optional<String> message1 = consensusBroker.receiveMessageFromLeader(TopicType.PREPARE_PHASE, String.valueOf(VIEW_NUMBER), list.get(0));
            assert (message1.isPresent());
            assertEquals(PREPARE_MESSAGE_LEADER, message1.get());
            consensusBroker.produceMessage(TopicType.PREPARE_PHASE, String.valueOf(VIEW_NUMBER), PREPARE_MESSAGE_LEADER);


            Optional<String> message2 = consensusBroker.receiveMessageFromLeader(TopicType.COMMITTEE_PHASE, String.valueOf(VIEW_NUMBER), list.get(0));
            assert (message2.isPresent());
            assertEquals(COMMITTEE_MESSAGE_LEADER, message2.get());
            consensusBroker.produceMessage(TopicType.COMMITTEE_PHASE, String.valueOf(VIEW_NUMBER), COMMITTEE_MESSAGE_LEADER);
        }
    }
}
