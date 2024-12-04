package io.Adrestus.Streaming;

import io.Adrestus.config.KafkaConfiguration;
import io.Adrestus.network.ConsensusBroker;
import io.Adrestus.network.TopicType;
import lombok.SneakyThrows;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.sql.SQLOutput;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class KafkaMultiNodeTest {

    private static final String ANNOUNCE_MESSAGE_VALIDATORS = "ANNOUNCE_MESSAGE_FROM_VALIDATORS";
    private static final String PREPARE_MESSAGE_VALIDATORS = "PREPARE_MESSAGE_FROM_VALIDATORS";
    private static final String COMMITTEE_MESSAGE_VALIDATORS = "COMMITTEE_MESSAGE_FROM_VALIDATORS";

    private static final String DISPERSE_MESSAGE_LEADER_CHUNK1 = "DISPERSE_MESSAGE_LEADER_CHUNK1";
    private static final String DISPERSE_MESSAGE_LEADER_CHUNK2 = "DISPERSE_MESSAGE_LEADER_CHUNK2";

    private static final String ANNOUNCE_MESSAGE_LEADER = "ANNOUNCE_MESSAGE_FROM_LEADER";
    private static final String PREPARE_MESSAGE_LEADER = "PREPARE_MESSAGE_FROM_LEADER";
    private static final String COMMITTEE_MESSAGE_LEADER = "COMMITTEE_MESSAGE_FROM_LEADER";

    private static final int CONSENSUS_VIEW_NUMBER = 1;
    private static final int DISPERSE_VIEW_NUMBER = 2;

    private static ArrayList<String> list;
    private static ConsensusBroker consensusBroker;
    private static OptionalInt position;

    @SneakyThrows
    @BeforeAll
    public static void setup() throws IOException {
        if (System.out.getClass().getName().contains("maven")) {
            System.out.println("Running from Maven: ");
            return;
        }
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress("google.com", 80));
        String IP = socket.getLocalAddress().getHostAddress();
        KafkaConfiguration.KAFKA_HOST = IP;
        list = new ArrayList<>();
        list.add("192.168.1.106");
        list.add("192.168.1.116");
        list.add("192.168.1.115");
        position = IntStream.range(0, list.size()).filter(i -> IP.equals(list.get(i))).findFirst();
        consensusBroker = new ConsensusBroker(list, list.get(0), position.getAsInt());
        consensusBroker.initializeKafkaKingdom();
    }


    // This is the daufault test you can test speed and if everything runs fine
    @SneakyThrows
    @Test
    @Order(2)
    public void testKafkaConsensusDispersePhases() {
        if (System.out.getClass().getName().contains("maven")) {
            System.out.println("Running from Maven: ");
            return;
        }

        if (position.getAsInt() == 0) {
            consensusBroker.produceMessage(TopicType.DISPERSE_PHASE1, 1, String.valueOf(DISPERSE_VIEW_NUMBER) + 1, DISPERSE_MESSAGE_LEADER_CHUNK1.getBytes(StandardCharsets.UTF_8));
            consensusBroker.produceMessage(TopicType.DISPERSE_PHASE1, 2, String.valueOf(DISPERSE_VIEW_NUMBER) + 2, DISPERSE_MESSAGE_LEADER_CHUNK2.getBytes(StandardCharsets.UTF_8));
            consensusBroker.flush();
        } else {
            if (position.getAsInt() == 1) {
                Optional<byte[]> message = consensusBroker.receiveDisperseMessageFromLeader(TopicType.DISPERSE_PHASE1, String.valueOf(DISPERSE_VIEW_NUMBER) + 1);
                assert (message.isPresent());
                System.out.println("received");
                assertEquals(DISPERSE_MESSAGE_LEADER_CHUNK1, new String(message.get()));
                consensusBroker.produceMessage(TopicType.DISPERSE_PHASE2, String.valueOf(DISPERSE_VIEW_NUMBER), DISPERSE_MESSAGE_LEADER_CHUNK1.getBytes(StandardCharsets.UTF_8));
                consensusBroker.flush();
                List<byte[]> res = consensusBroker.receiveMessageFromValidators(TopicType.DISPERSE_PHASE2, String.valueOf(DISPERSE_VIEW_NUMBER));
                System.out.println("received2");
                assert (!res.isEmpty());
                assertEquals(DISPERSE_MESSAGE_LEADER_CHUNK2, new String(res.get(0)));
            } else {
                Optional<byte[]> message = consensusBroker.receiveDisperseMessageFromLeader(TopicType.DISPERSE_PHASE1, String.valueOf(DISPERSE_VIEW_NUMBER) + 2);
                assert (message.isPresent());
                System.out.println("received");
                assertEquals(DISPERSE_MESSAGE_LEADER_CHUNK2, new String(message.get()));
                consensusBroker.produceMessage(TopicType.DISPERSE_PHASE2, String.valueOf(DISPERSE_VIEW_NUMBER), DISPERSE_MESSAGE_LEADER_CHUNK2.getBytes(StandardCharsets.UTF_8));
                consensusBroker.flush();
                List<byte[]> res = consensusBroker.receiveMessageFromValidators(TopicType.DISPERSE_PHASE2, String.valueOf(DISPERSE_VIEW_NUMBER));
                System.out.println("received2");
                assert (!res.isEmpty());
                assertEquals(DISPERSE_MESSAGE_LEADER_CHUNK1, new String(res.get(0)));
            }
        }
        System.out.println("Test Passed");
        Thread.sleep(4000);
    }

    @SneakyThrows
    @Test
    @Order(1)
    public void testKafkaConsensusThreePhases() {
        if (System.out.getClass().getName().contains("maven")) {
            System.out.println("Running from Maven: ");
            return;
        }
        if (position.getAsInt() == 0) {
            assertEquals(1,consensusBroker.getSequencedMap().get(TopicType.ANNOUNCE_PHASE).size());
            assertEquals(1,consensusBroker.getSequencedMap().get(TopicType.PREPARE_PHASE).size());
            assertEquals(1,consensusBroker.getSequencedMap().get(TopicType.COMMITTEE_PHASE).size());
            assertEquals(1,consensusBroker.getSequencedMap().get(TopicType.DISPERSE_PHASE2).size());

            assertEquals(0,consensusBroker.getSequencedMap().get(TopicType.ANNOUNCE_PHASE).firstEntry().getKey());
            assertEquals(0,consensusBroker.getSequencedMap().get(TopicType.PREPARE_PHASE).firstEntry().getKey());
            assertEquals(0,consensusBroker.getSequencedMap().get(TopicType.COMMITTEE_PHASE).firstEntry().getKey());
            assertEquals(0,consensusBroker.getSequencedMap().get(TopicType.DISPERSE_PHASE2).firstEntry().getKey());

            assertEquals(2,consensusBroker.getSequencedMap().get(TopicType.ANNOUNCE_PHASE).firstEntry().getValue().size());
            assertEquals(2,consensusBroker.getSequencedMap().get(TopicType.PREPARE_PHASE).firstEntry().getValue().size());
            assertEquals(2,consensusBroker.getSequencedMap().get(TopicType.COMMITTEE_PHASE).firstEntry().getValue().size());
            assertEquals(2,consensusBroker.getSequencedMap().get(TopicType.DISPERSE_PHASE2).firstEntry().getValue().size());

            consensusBroker.getSequencedMap().get(TopicType.ANNOUNCE_PHASE).firstEntry().getValue().values().forEach(value -> assertEquals("0", new String(value)));
            consensusBroker.getSequencedMap().get(TopicType.PREPARE_PHASE).firstEntry().getValue().values().forEach(value -> assertEquals("0", new String(value)));
            consensusBroker.getSequencedMap().get(TopicType.COMMITTEE_PHASE).firstEntry().getValue().values().forEach(value -> assertEquals("0", new String(value)));
            consensusBroker.getSequencedMap().get(TopicType.DISPERSE_PHASE2).firstEntry().getValue().values().forEach(value -> assertEquals("0", new String(value)));

            consensusBroker.produceMessage(TopicType.ANNOUNCE_PHASE, String.valueOf(CONSENSUS_VIEW_NUMBER), ANNOUNCE_MESSAGE_LEADER.getBytes(StandardCharsets.UTF_8));
            List<byte[]> res = consensusBroker.receiveMessageFromValidators(TopicType.ANNOUNCE_PHASE, String.valueOf(CONSENSUS_VIEW_NUMBER));
            res.forEach(val -> assertEquals(ANNOUNCE_MESSAGE_VALIDATORS, new String(val)));
            System.out.println(TopicType.ANNOUNCE_PHASE.name() + " Received from validators: " + res);
            consensusBroker.produceMessage(TopicType.PREPARE_PHASE, String.valueOf(CONSENSUS_VIEW_NUMBER), PREPARE_MESSAGE_LEADER.getBytes(StandardCharsets.UTF_8));
            List<byte[]> res1 = consensusBroker.receiveMessageFromValidators(TopicType.PREPARE_PHASE, String.valueOf(CONSENSUS_VIEW_NUMBER));
            res1.forEach(val -> assertEquals(PREPARE_MESSAGE_VALIDATORS, new String(val)));
            System.out.println(TopicType.PREPARE_PHASE.name() + " Received from validators: " + res1);
            consensusBroker.produceMessage(TopicType.COMMITTEE_PHASE, String.valueOf(CONSENSUS_VIEW_NUMBER), COMMITTEE_MESSAGE_LEADER.getBytes(StandardCharsets.UTF_8));
            List<byte[]> res3 = consensusBroker.receiveMessageFromValidators(TopicType.COMMITTEE_PHASE, String.valueOf(CONSENSUS_VIEW_NUMBER));
            res3.forEach(val -> assertEquals(COMMITTEE_MESSAGE_VALIDATORS, new String(val)));
            System.out.println(TopicType.COMMITTEE_PHASE.name() + " Received from validators: " + res3);

            assertEquals(2,consensusBroker.getSequencedMap().get(TopicType.ANNOUNCE_PHASE).size());
            assertEquals(2,consensusBroker.getSequencedMap().get(TopicType.PREPARE_PHASE).size());
            assertEquals(2,consensusBroker.getSequencedMap().get(TopicType.COMMITTEE_PHASE).size());
            assertEquals(1,consensusBroker.getSequencedMap().get(TopicType.DISPERSE_PHASE2).size());

            assertEquals(1,consensusBroker.getSequencedMap().get(TopicType.ANNOUNCE_PHASE).firstEntry().getKey());
            assertEquals(1,consensusBroker.getSequencedMap().get(TopicType.PREPARE_PHASE).firstEntry().getKey());
            assertEquals(1,consensusBroker.getSequencedMap().get(TopicType.COMMITTEE_PHASE).firstEntry().getKey());
            assertEquals(0,consensusBroker.getSequencedMap().get(TopicType.DISPERSE_PHASE2).firstEntry().getKey());

            assertEquals(2,consensusBroker.getSequencedMap().get(TopicType.ANNOUNCE_PHASE).firstEntry().getValue().size());
            assertEquals(2,consensusBroker.getSequencedMap().get(TopicType.PREPARE_PHASE).firstEntry().getValue().size());
            assertEquals(2,consensusBroker.getSequencedMap().get(TopicType.COMMITTEE_PHASE).firstEntry().getValue().size());
            assertEquals(2,consensusBroker.getSequencedMap().get(TopicType.DISPERSE_PHASE2).firstEntry().getValue().size());

            consensusBroker.getSequencedMap().get(TopicType.ANNOUNCE_PHASE).firstEntry().getValue().values().forEach(value -> assertEquals(ANNOUNCE_MESSAGE_VALIDATORS, new String(value)));
            consensusBroker.getSequencedMap().get(TopicType.PREPARE_PHASE).firstEntry().getValue().values().forEach(value -> assertEquals(PREPARE_MESSAGE_VALIDATORS, new String(value)));
            consensusBroker.getSequencedMap().get(TopicType.COMMITTEE_PHASE).firstEntry().getValue().values().forEach(value -> assertEquals(COMMITTEE_MESSAGE_VALIDATORS, new String(value)));
            consensusBroker.getSequencedMap().get(TopicType.DISPERSE_PHASE2).firstEntry().getValue().values().forEach(value -> assertEquals("0", new String(value)));
        } else {
            assertEquals(1,consensusBroker.getSequencedMap().get(TopicType.ANNOUNCE_PHASE).size());
            assertEquals(1,consensusBroker.getSequencedMap().get(TopicType.PREPARE_PHASE).size());
            assertEquals(1,consensusBroker.getSequencedMap().get(TopicType.COMMITTEE_PHASE).size());
            assertEquals(1,consensusBroker.getSequencedMap().get(TopicType.DISPERSE_PHASE2).size());

            assertEquals(0,consensusBroker.getSequencedMap().get(TopicType.ANNOUNCE_PHASE).firstEntry().getKey());
            assertEquals(0,consensusBroker.getSequencedMap().get(TopicType.PREPARE_PHASE).firstEntry().getKey());
            assertEquals(0,consensusBroker.getSequencedMap().get(TopicType.COMMITTEE_PHASE).firstEntry().getKey());
            assertEquals(0,consensusBroker.getSequencedMap().get(TopicType.DISPERSE_PHASE2).firstEntry().getKey());

            assertEquals(1,consensusBroker.getSequencedMap().get(TopicType.ANNOUNCE_PHASE).firstEntry().getValue().size());
            assertEquals(1,consensusBroker.getSequencedMap().get(TopicType.PREPARE_PHASE).firstEntry().getValue().size());
            assertEquals(1,consensusBroker.getSequencedMap().get(TopicType.COMMITTEE_PHASE).firstEntry().getValue().size());
            assertEquals(1,consensusBroker.getSequencedMap().get(TopicType.DISPERSE_PHASE2).firstEntry().getValue().size());

            Optional<byte[]> message = consensusBroker.receiveMessageFromLeader(TopicType.ANNOUNCE_PHASE, String.valueOf(CONSENSUS_VIEW_NUMBER));
            assert (message.isPresent());
            assertEquals(ANNOUNCE_MESSAGE_LEADER, new String(message.get()));
            consensusBroker.produceMessage(TopicType.ANNOUNCE_PHASE, String.valueOf(CONSENSUS_VIEW_NUMBER), ANNOUNCE_MESSAGE_VALIDATORS.getBytes(StandardCharsets.UTF_8));

            Optional<byte[]> message1 = consensusBroker.receiveMessageFromLeader(TopicType.PREPARE_PHASE, String.valueOf(CONSENSUS_VIEW_NUMBER));
            assert (message1.isPresent());
            assertEquals(PREPARE_MESSAGE_LEADER, new String(message1.get()));
            consensusBroker.produceMessage(TopicType.PREPARE_PHASE, String.valueOf(CONSENSUS_VIEW_NUMBER), PREPARE_MESSAGE_VALIDATORS.getBytes(StandardCharsets.UTF_8));


            Optional<byte[]> message2 = consensusBroker.receiveMessageFromLeader(TopicType.COMMITTEE_PHASE, String.valueOf(CONSENSUS_VIEW_NUMBER));
            assert (message2.isPresent());
            assertEquals(COMMITTEE_MESSAGE_LEADER, new String(message2.get()));
            consensusBroker.produceMessage(TopicType.COMMITTEE_PHASE, String.valueOf(CONSENSUS_VIEW_NUMBER), COMMITTEE_MESSAGE_VALIDATORS.getBytes(StandardCharsets.UTF_8));

            assertEquals(1,consensusBroker.getSequencedMap().get(TopicType.ANNOUNCE_PHASE).firstEntry().getKey());
            assertEquals(1,consensusBroker.getSequencedMap().get(TopicType.PREPARE_PHASE).firstEntry().getKey());
            assertEquals(1,consensusBroker.getSequencedMap().get(TopicType.COMMITTEE_PHASE).firstEntry().getKey());
            assertEquals(0,consensusBroker.getSequencedMap().get(TopicType.DISPERSE_PHASE2).firstEntry().getKey());

            assertEquals(1,consensusBroker.getSequencedMap().get(TopicType.ANNOUNCE_PHASE).firstEntry().getValue().size());
            assertEquals(1,consensusBroker.getSequencedMap().get(TopicType.PREPARE_PHASE).firstEntry().getValue().size());
            assertEquals(1,consensusBroker.getSequencedMap().get(TopicType.COMMITTEE_PHASE).firstEntry().getValue().size());
            assertEquals(1,consensusBroker.getSequencedMap().get(TopicType.DISPERSE_PHASE2).firstEntry().getValue().size());

            consensusBroker.getSequencedMap().get(TopicType.ANNOUNCE_PHASE).firstEntry().getValue().values().forEach(value -> assertEquals(ANNOUNCE_MESSAGE_LEADER, new String(value)));
            consensusBroker.getSequencedMap().get(TopicType.PREPARE_PHASE).firstEntry().getValue().values().forEach(value -> assertEquals(PREPARE_MESSAGE_LEADER, new String(value)));
            consensusBroker.getSequencedMap().get(TopicType.COMMITTEE_PHASE).firstEntry().getValue().values().forEach(value -> assertEquals(COMMITTEE_MESSAGE_LEADER, new String(value)));
            consensusBroker.getSequencedMap().get(TopicType.DISPERSE_PHASE2).firstEntry().getValue().values().forEach(value -> assertEquals("0", new String(value)));
        }
        System.out.println("Test Passed");
    }

    @SneakyThrows
    @AfterAll
    public static void tearDown() {
        if (System.out.getClass().getName().contains("maven")) {
            System.out.println("Running from Maven: ");
            return;
        }
        Thread.sleep(8000);
        consensusBroker.shutDownGracefully();
    }
}
