package io.Adrestus.Streaming;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.OutputStreamAppender;
import io.Adrestus.config.KafkaConfiguration;
import io.Adrestus.network.ConsensusBroker;
import io.Adrestus.network.TopicType;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class KafkaMultiNodeFullTest {
    private static final Logger LOG = LoggerFactory.getLogger(KafkaMultiNodeFullTest.class);

    private static final String MATCH = "Duplicate-";
    private static final String ANNOUNCE_MESSAGE_VALIDATORS = "ANNOUNCE_MESSAGE_FROM_VALIDATORS";
    private static final String PREPARE_MESSAGE_VALIDATORS = "PREPARE_MESSAGE_FROM_VALIDATORS";
    private static final String COMMITTEE_MESSAGE_VALIDATORS = "COMMITTEE_MESSAGE_FROM_VALIDATORS";
    private static final String DISPERSE2_MESSAGE_VALIDATORS = "DISPERSE2_MESSAGE_VALIDATORS";

    private static final String DISPERSE_MESSAGE_LEADER_CHUNK1 = "DISPERSE_MESSAGE_LEADER_CHUNK1";
    private static final String DISPERSE_MESSAGE_LEADER_CHUNK2 = "DISPERSE_MESSAGE_LEADER_CHUNK2";

    private static final String ANNOUNCE_MESSAGE_LEADER = "ANNOUNCE_MESSAGE_FROM_LEADER";
    private static final String PREPARE_MESSAGE_LEADER = "PREPARE_MESSAGE_FROM_LEADER";
    private static final String COMMITTEE_MESSAGE_LEADER = "COMMITTEE_MESSAGE_FROM_LEADER";

    private static final int ITERATIONS_MAX = 18;

    private static ArrayList<String> list;
    private static ConsensusBroker consensusBroker;
    private static OptionalInt current_position;
    private static int leader_position;
    // Create a ByteArrayOutputStream to capture log output
    private static ByteArrayOutputStream baos = new ByteArrayOutputStream();
    private static ch.qos.logback.classic.Logger rootLogger;
    private static OutputStreamAppender<ILoggingEvent> appender;

    @SneakyThrows
    @BeforeAll
    public static void setup() throws IOException {
        if (System.out.getClass().getName().contains("maven")) {
            System.out.println("Running from Maven: ");
            return;
        }


        // Get the LoggerContext and root logger
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        rootLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);

        // Create an OutputStreamAppender
        appender = new OutputStreamAppender<>();
        appender.setContext(loggerContext);
        appender.setOutputStream(baos);

        // Create a PatternLayoutEncoder
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(loggerContext);
        encoder.setPattern("%msg%n");
        encoder.start();

        // Set the encoder on the appender and start it
        appender.setEncoder(encoder);
        appender.start();

        // Add the appender to the root logger
        rootLogger.addAppender(appender);


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
        int count = 1;
        while (count <= ITERATIONS_MAX) {
            if (leader_position == current_position.getAsInt()) {
                System.out.println("leader iteration: " + count);
                long start = System.currentTimeMillis();
                consensusBroker.seekDisperseOffsetToEnd();

                //this sleep must be time to construct erasure code
                Thread.sleep(10);
                for (int i = 0; i < list.size(); i++) {
                    if (i == leader_position) {
                        continue;
                    } else if (i == 1) {
                        consensusBroker.produceMessage(TopicType.DISPERSE_PHASE1, i, String.valueOf(count), DISPERSE_MESSAGE_LEADER_CHUNK1.getBytes(StandardCharsets.UTF_8));
                    } else {
                        consensusBroker.produceMessage(TopicType.DISPERSE_PHASE1, i, String.valueOf(count), DISPERSE_MESSAGE_LEADER_CHUNK2.getBytes(StandardCharsets.UTF_8));
                    }
                }
                consensusBroker.flush();
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

                var map = consensusBroker.getSequencedMap();
                assertEquals(count + 1, consensusBroker.getSequencedMap().get(TopicType.ANNOUNCE_PHASE).size());
                assertEquals(count + 1, consensusBroker.getSequencedMap().get(TopicType.PREPARE_PHASE).size());
                assertEquals(count + 1, consensusBroker.getSequencedMap().get(TopicType.COMMITTEE_PHASE).size());
//                assertEquals(count, consensusBroker.getSequencedMap().get(TopicType.DISPERSE_PHASE2).size());

                assertEquals(count, consensusBroker.getSequencedMap().get(TopicType.ANNOUNCE_PHASE).firstEntry().getKey());
                assertEquals(count, consensusBroker.getSequencedMap().get(TopicType.PREPARE_PHASE).firstEntry().getKey());
                assertEquals(count, consensusBroker.getSequencedMap().get(TopicType.COMMITTEE_PHASE).firstEntry().getKey());
                assertEquals(count - 1, consensusBroker.getSequencedMap().get(TopicType.DISPERSE_PHASE2).firstEntry().getKey());

                assertEquals(2, consensusBroker.getSequencedMap().get(TopicType.ANNOUNCE_PHASE).firstEntry().getValue().size());
                assertEquals(2, consensusBroker.getSequencedMap().get(TopicType.PREPARE_PHASE).firstEntry().getValue().size());
                assertEquals(2, consensusBroker.getSequencedMap().get(TopicType.COMMITTEE_PHASE).firstEntry().getValue().size());

                consensusBroker.getSequencedMap().get(TopicType.ANNOUNCE_PHASE).firstEntry().getValue().values().forEach(value -> assertEquals(ANNOUNCE_MESSAGE_VALIDATORS, new String(value)));
                consensusBroker.getSequencedMap().get(TopicType.PREPARE_PHASE).firstEntry().getValue().values().forEach(value -> assertEquals(PREPARE_MESSAGE_VALIDATORS, new String(value)));
                consensusBroker.getSequencedMap().get(TopicType.COMMITTEE_PHASE).firstEntry().getValue().values().forEach(value -> assertEquals(COMMITTEE_MESSAGE_VALIDATORS, new String(value)));
                long finish = System.currentTimeMillis();
                long timeElapsed = finish - 10 - start;
                elaspe_time.add(timeElapsed);
                count++;
                Thread.sleep(1000);
            } else {
                System.out.println("Validator iteration: " + count);
                Optional<byte[]> message0 = consensusBroker.receiveDisperseMessageFromLeader(TopicType.DISPERSE_PHASE1, String.valueOf(count));
                assertEquals(1, consensusBroker.getSequencedMap().get(TopicType.DISPERSE_PHASE1).firstEntry().getValue().size());
                assert (message0.isPresent());
                if (current_position.getAsInt() == 1) {
                    assertEquals(DISPERSE_MESSAGE_LEADER_CHUNK1, new String(message0.get()));
                } else if (current_position.getAsInt() == 2) {
                    assertEquals(DISPERSE_MESSAGE_LEADER_CHUNK2, new String(message0.get()));
                }

                consensusBroker.produceMessage(TopicType.DISPERSE_PHASE2, String.valueOf(count), DISPERSE2_MESSAGE_VALIDATORS.getBytes(StandardCharsets.UTF_8));
                List<byte[]> message_disperse = consensusBroker.receiveMessageFromValidators(TopicType.DISPERSE_PHASE2, String.valueOf(count));
                message_disperse.forEach(val -> assertEquals(DISPERSE2_MESSAGE_VALIDATORS, new String(val)));

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
                consensusBroker.flush();
                count++;
                Thread.sleep(1000);
                consensusBroker.seekAllOffsetToEnd();

            }
            consensusBroker.clear();
            leader_position++;
            if (leader_position == list.size()) {
                leader_position = 0;
            }
            consensusBroker.setLeader_host(list.get(leader_position));
            consensusBroker.setLeader_position(leader_position);
        }

        var map = consensusBroker.getSequencedMap();
        var list = elaspe_time;
        OptionalDouble average = elaspe_time.stream().skip(1).mapToLong(Long::longValue).average();
        if (average.isPresent()) {
            System.out.println("Average elapsed time: " + average.getAsDouble() + " ms");
        } else {
            System.out.println("No elapsed time recorded.");
        }

        // Convert the log output to a string
        String logOutput = baos.toString();

        // Check if the string contains "duplicate exists"
        if (logOutput.contains(MATCH)) {
            throw new IllegalArgumentException("The string 'duplicate exists' was found in the log output.");
        }
        // Remove the appender from the root logger
        rootLogger.detachAppender(appender);
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
