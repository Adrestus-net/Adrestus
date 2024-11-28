package io.Adrestus.Streaming;

import io.Adrestus.config.KafkaConfiguration;
import io.Adrestus.network.ConsensusBroker;
import io.Adrestus.network.TopicType;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class KafkaMultiNodeFullTest {
    private static final String ANNOUNCE_MESSAGE_VALIDATORS = "ANNOUNCE_MESSAGE_FROM_VALIDATORS";
    private static final String PREPARE_MESSAGE_VALIDATORS = "PREPARE_MESSAGE_FROM_VALIDATORS";
    private static final String COMMITTEE_MESSAGE_VALIDATORS = "COMMITTEE_MESSAGE_FROM_VALIDATORS";

    private static final String DISPERSE_MESSAGE_LEADER_CHUNK1 = "DISPERSE_MESSAGE_LEADER_CHUNK1";
    private static final String DISPERSE_MESSAGE_LEADER_CHUNK2 = "DISPERSE_MESSAGE_LEADER_CHUNK2";

    private static final String ANNOUNCE_MESSAGE_LEADER = "ANNOUNCE_MESSAGE_FROM_LEADER";
    private static final String PREPARE_MESSAGE_LEADER = "PREPARE_MESSAGE_FROM_LEADER";
    private static final String COMMITTEE_MESSAGE_LEADER = "COMMITTEE_MESSAGE_FROM_LEADER";

    private static final int ITERATIONS_MAX = 10;


    private static ArrayList<String> list;
    private static ConsensusBroker consensusBroker;
    private static OptionalInt current_position;
    private static int leader_position;

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
        leader_position = 0;
        current_position = IntStream.range(0, list.size()).filter(i -> IP.equals(list.get(i))).findFirst();
        consensusBroker = new ConsensusBroker(list, list.get(0), current_position.getAsInt());
        consensusBroker.initializeKafkaKingdom();
    }


    @SneakyThrows
    @Test
    public void test() {
        if (System.out.getClass().getName().contains("maven")) {
            System.out.println("Running from Maven: ");
            return;
        }

        ArrayList<Long> elaspe_time = new ArrayList<>();
        int count = 0;
        while (count < ITERATIONS_MAX) {
            if (leader_position == current_position.getAsInt()) {
                long start = System.currentTimeMillis();
                for (int i = 0; i < list.size(); i++) {
                    if (i == leader_position) {
                        continue;
                    }
                    consensusBroker.produceMessage(TopicType.DISPERSE_PHASE1, i, String.valueOf(count), DISPERSE_MESSAGE_LEADER_CHUNK1.getBytes(StandardCharsets.UTF_8));
                    consensusBroker.produceMessage(TopicType.DISPERSE_PHASE1, i, String.valueOf(count), DISPERSE_MESSAGE_LEADER_CHUNK2.getBytes(StandardCharsets.UTF_8));
                }
                consensusBroker.flush();

                //consensusBroker.seekOffsetToEnd();
                consensusBroker.produceMessage(TopicType.ANNOUNCE_PHASE, String.valueOf(count), ANNOUNCE_MESSAGE_LEADER.getBytes(StandardCharsets.UTF_8));
                List<byte[]> res = consensusBroker.receiveMessageFromValidators(TopicType.ANNOUNCE_PHASE, String.valueOf(count));
                res.forEach(val -> assertEquals(ANNOUNCE_MESSAGE_VALIDATORS, new String(val)));
                System.out.println(TopicType.ANNOUNCE_PHASE.name() + " Received from validators: " + res);
                consensusBroker.produceMessage(TopicType.PREPARE_PHASE, String.valueOf(count), PREPARE_MESSAGE_LEADER.getBytes(StandardCharsets.UTF_8));
                List<byte[]> res1 = consensusBroker.receiveMessageFromValidators(TopicType.PREPARE_PHASE, String.valueOf(count));
                res1.forEach(val -> assertEquals(PREPARE_MESSAGE_VALIDATORS, new String(val)));
                System.out.println(TopicType.PREPARE_PHASE.name() + " Received from validators: " + res1);
                consensusBroker.produceMessage(TopicType.COMMITTEE_PHASE, String.valueOf(count), COMMITTEE_MESSAGE_LEADER.getBytes(StandardCharsets.UTF_8));
                List<byte[]> res3 = consensusBroker.receiveMessageFromValidators(TopicType.COMMITTEE_PHASE, String.valueOf(count));
                res3.forEach(val -> assertEquals(COMMITTEE_MESSAGE_VALIDATORS, new String(val)));
                System.out.println(TopicType.COMMITTEE_PHASE.name() + " Received from validators: " + res3);

                assertEquals(count + 1, consensusBroker.getSequencedMap().get(TopicType.ANNOUNCE_PHASE).size());
                assertEquals(count + 1, consensusBroker.getSequencedMap().get(TopicType.PREPARE_PHASE).size());
                assertEquals(count + 1, consensusBroker.getSequencedMap().get(TopicType.COMMITTEE_PHASE).size());
                //assertEquals(count, consensusBroker.getSequencedMap().get(TopicType.DISPERSE_PHASE2).size());

                assertEquals(count, consensusBroker.getSequencedMap().get(TopicType.ANNOUNCE_PHASE).firstEntry().getKey());
                assertEquals(count, consensusBroker.getSequencedMap().get(TopicType.PREPARE_PHASE).firstEntry().getKey());
                assertEquals(count, consensusBroker.getSequencedMap().get(TopicType.COMMITTEE_PHASE).firstEntry().getKey());
                //assertEquals(count - 1, consensusBroker.getSequencedMap().get(TopicType.DISPERSE_PHASE2).firstEntry().getKey());

                assertEquals(2, consensusBroker.getSequencedMap().get(TopicType.ANNOUNCE_PHASE).firstEntry().getValue().size());
                assertEquals(2, consensusBroker.getSequencedMap().get(TopicType.PREPARE_PHASE).firstEntry().getValue().size());
                assertEquals(2, consensusBroker.getSequencedMap().get(TopicType.COMMITTEE_PHASE).firstEntry().getValue().size());

                consensusBroker.getSequencedMap().get(TopicType.ANNOUNCE_PHASE).firstEntry().getValue().values().forEach(value -> assertEquals(ANNOUNCE_MESSAGE_VALIDATORS, new String(value)));
                consensusBroker.getSequencedMap().get(TopicType.PREPARE_PHASE).firstEntry().getValue().values().forEach(value -> assertEquals(PREPARE_MESSAGE_VALIDATORS, new String(value)));
                consensusBroker.getSequencedMap().get(TopicType.COMMITTEE_PHASE).firstEntry().getValue().values().forEach(value -> assertEquals(COMMITTEE_MESSAGE_VALIDATORS, new String(value)));
                long finish = System.currentTimeMillis();
                long timeElapsed = finish - start;
                elaspe_time.add(timeElapsed);
            } else {
                Optional<byte[]> message0 = consensusBroker.receiveDisperseMessageFromLeader(TopicType.DISPERSE_PHASE1, String.valueOf(count));
                assert (message0.isPresent());
                assertEquals(ANNOUNCE_MESSAGE_LEADER, new String(message0.get()));
                consensusBroker.produceMessage(TopicType.DISPERSE_PHASE2, "0", "0".getBytes(StandardCharsets.UTF_8));
                Optional<byte[]> message = consensusBroker.receiveMessageFromLeader(TopicType.ANNOUNCE_PHASE, String.valueOf(count));
                assert (message.isPresent());
                assertEquals(ANNOUNCE_MESSAGE_LEADER, new String(message.get()));
                consensusBroker.produceMessage(TopicType.ANNOUNCE_PHASE, String.valueOf(count), ANNOUNCE_MESSAGE_VALIDATORS.getBytes(StandardCharsets.UTF_8));

                Optional<byte[]> message1 = consensusBroker.receiveMessageFromLeader(TopicType.PREPARE_PHASE, String.valueOf(count));
                assert (message1.isPresent());
                assertEquals(PREPARE_MESSAGE_LEADER, new String(message1.get()));
                consensusBroker.produceMessage(TopicType.PREPARE_PHASE, String.valueOf(count), PREPARE_MESSAGE_VALIDATORS.getBytes(StandardCharsets.UTF_8));


                Optional<byte[]> message2 = consensusBroker.receiveMessageFromLeader(TopicType.COMMITTEE_PHASE, String.valueOf(count));
                assert (message2.isPresent());
                assertEquals(COMMITTEE_MESSAGE_LEADER, new String(message2.get()));
                consensusBroker.produceMessage(TopicType.COMMITTEE_PHASE, String.valueOf(count), COMMITTEE_MESSAGE_VALIDATORS.getBytes(StandardCharsets.UTF_8));

                consensusBroker.seekOffsetToEnd();
            }
            count++;
            Thread.sleep(1000);
        }

        OptionalDouble average = elaspe_time.stream().mapToLong(Long::longValue).average();
        if (average.isPresent()) {
            System.out.println("Average elapsed time: " + average.getAsDouble() + " ms");
        } else {
            System.out.println("No elapsed time recorded.");
        }
    }
}
