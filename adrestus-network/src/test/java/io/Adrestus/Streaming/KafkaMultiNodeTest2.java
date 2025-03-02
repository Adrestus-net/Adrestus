package io.Adrestus.Streaming;

import io.Adrestus.config.KafkaConfiguration;
import io.Adrestus.config.RunningConfig;
import io.Adrestus.network.ConsensusBroker;
import io.Adrestus.network.TopicType;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;


public class KafkaMultiNodeTest2 {

    private static final int VIEW_NUMBER = 1;
    private static final int byteArraySize = 16;

    private static ArrayList<String> list;
    private static ConsensusBroker consensusBroker;
    private static OptionalInt position;
    private static byte[] randomBytes;

    @SneakyThrows
    @BeforeAll
    public static void setup() throws IOException {
        if (RunningConfig.isRunningInMaven() && !RunningConfig.isRunningInDocker() && RunningConfig.isRunningInAppveyor()) {
            return;
        } else if (RunningConfig.isRunningInMaven() && !RunningConfig.isRunningInDocker() && !RunningConfig.isRunningInAppveyor()) {
            return;
        } else if ((RunningConfig.isRunningInDocker() && !RunningConfig.isRunningInAppveyor()) || !RunningConfig.isRunningInMaven()) {
            SecureRandom secureRandom = new SecureRandom();// Example: 16 bytes
            randomBytes = new byte[byteArraySize];
            secureRandom.nextBytes(randomBytes);

            Socket socket = new Socket();
            socket.connect(new InetSocketAddress("google.com", 80));
            String IP = socket.getLocalAddress().getHostAddress();
            KafkaConfiguration.KAFKA_HOST = IP;
            list = new ArrayList<>();
            list.add(System.getProperty("test.arg0") == null ? "192.168.1.106" : System.getProperty("test.arg0"));
            list.add(System.getProperty("test.arg1") == null ? "192.168.1.116" : System.getProperty("test.arg1"));
            list.add(System.getProperty("test.arg2") == null ? "192.168.1.115" : System.getProperty("test.arg2"));
            position = IntStream.range(0, list.size()).filter(i -> IP.equals(list.get(i))).findFirst();
            consensusBroker = new ConsensusBroker(list, list.get(0), position.getAsInt());
            consensusBroker.initializeKafkaKingdom();
        }
    }


    // This is the test increase message size you can test speed and if everything runs fine
    @SneakyThrows
    @Test
    public void testKafkaConsensusDispersePhases() {
        if (RunningConfig.isRunningInMaven() && !RunningConfig.isRunningInDocker() && RunningConfig.isRunningInAppveyor()) {
            return;
        } else if (RunningConfig.isRunningInMaven() && !RunningConfig.isRunningInDocker() && !RunningConfig.isRunningInAppveyor()) {
            return;
        } else if ((RunningConfig.isRunningInDocker() && !RunningConfig.isRunningInAppveyor()) || !RunningConfig.isRunningInMaven()) {
            if (position.getAsInt() == 0) {
                consensusBroker.produceMessage(TopicType.DISPERSE_PHASE1, 1, String.valueOf(VIEW_NUMBER) + 1, randomBytes);
                consensusBroker.produceMessage(TopicType.DISPERSE_PHASE1, 2, String.valueOf(VIEW_NUMBER) + 2, randomBytes);
                Thread.sleep(6000);
            } else {
                if (position.getAsInt() == 1) {
                    Optional<byte[]> message = consensusBroker.receiveDisperseMessageFromLeader(TopicType.DISPERSE_PHASE1, String.valueOf(VIEW_NUMBER) + 1);
                    assert (message.isPresent());
                    System.out.println("received");
                    assertNotNull(message.get());
                    consensusBroker.produceMessage(TopicType.DISPERSE_PHASE2, String.valueOf(VIEW_NUMBER), randomBytes);
                    List<byte[]> res = consensusBroker.receiveMessageFromValidators(TopicType.DISPERSE_PHASE2, String.valueOf(VIEW_NUMBER));
                    System.out.println("received2");
                    assert (!res.isEmpty());
                } else {
                    Optional<byte[]> message = consensusBroker.receiveDisperseMessageFromLeader(TopicType.DISPERSE_PHASE1, String.valueOf(VIEW_NUMBER) + 2);
                    assert (message.isPresent());
                    System.out.println("received");
                    assertNotNull(message.get());
                    consensusBroker.produceMessage(TopicType.DISPERSE_PHASE2, String.valueOf(VIEW_NUMBER), randomBytes);
                    List<byte[]> res = consensusBroker.receiveMessageFromValidators(TopicType.DISPERSE_PHASE2, String.valueOf(VIEW_NUMBER));
                    System.out.println("received2");
                    assert (!res.isEmpty());
                    Thread.sleep(4000);
                }
            }
        }
    }

    @SneakyThrows
    @Test
    public void testKafkaConsensusThreePhases() {
        if (RunningConfig.isRunningInMaven() && !RunningConfig.isRunningInDocker() && RunningConfig.isRunningInAppveyor()) {
            return;
        } else if (RunningConfig.isRunningInMaven() && !RunningConfig.isRunningInDocker() && !RunningConfig.isRunningInAppveyor()) {
            return;
        } else if ((RunningConfig.isRunningInDocker() && !RunningConfig.isRunningInAppveyor()) || !RunningConfig.isRunningInMaven()) {
            if (position.getAsInt() == 0) {
                consensusBroker.produceMessage(TopicType.ANNOUNCE_PHASE, String.valueOf(VIEW_NUMBER), randomBytes);
                List<byte[]> res = consensusBroker.receiveMessageFromValidators(TopicType.ANNOUNCE_PHASE, String.valueOf(VIEW_NUMBER));
                assertFalse(res.isEmpty());
                System.out.println(TopicType.ANNOUNCE_PHASE.name() + " Received from validators: " + res);
                consensusBroker.produceMessage(TopicType.PREPARE_PHASE, String.valueOf(VIEW_NUMBER), randomBytes);
                List<byte[]> res1 = consensusBroker.receiveMessageFromValidators(TopicType.PREPARE_PHASE, String.valueOf(VIEW_NUMBER));
                assertFalse(res1.isEmpty());
                System.out.println(TopicType.PREPARE_PHASE.name() + " Received from validators: " + res1);
                consensusBroker.produceMessage(TopicType.COMMITTEE_PHASE, String.valueOf(VIEW_NUMBER), randomBytes);
                List<byte[]> res3 = consensusBroker.receiveMessageFromValidators(TopicType.COMMITTEE_PHASE, String.valueOf(VIEW_NUMBER));
                assertFalse(res3.isEmpty());
                System.out.println(TopicType.COMMITTEE_PHASE.name() + " Received from validators: " + res3);
            } else {
                Optional<byte[]> message = consensusBroker.receiveMessageFromLeader(TopicType.ANNOUNCE_PHASE, String.valueOf(VIEW_NUMBER));
                assert (message.isPresent());
                consensusBroker.produceMessage(TopicType.ANNOUNCE_PHASE, String.valueOf(VIEW_NUMBER), randomBytes);

                Optional<byte[]> message1 = consensusBroker.receiveMessageFromLeader(TopicType.PREPARE_PHASE, String.valueOf(VIEW_NUMBER));
                assert (message1.isPresent());
                consensusBroker.produceMessage(TopicType.PREPARE_PHASE, String.valueOf(VIEW_NUMBER), randomBytes);


                Optional<byte[]> message2 = consensusBroker.receiveMessageFromLeader(TopicType.COMMITTEE_PHASE, String.valueOf(VIEW_NUMBER));
                assert (message2.isPresent());
                consensusBroker.produceMessage(TopicType.COMMITTEE_PHASE, String.valueOf(VIEW_NUMBER), randomBytes);
            }
        }
    }

    @SneakyThrows
    @AfterAll
    public static void tearDown() {
        if (RunningConfig.isRunningInMaven() && !RunningConfig.isRunningInDocker() && RunningConfig.isRunningInAppveyor()) {
            return;
        } else if (RunningConfig.isRunningInMaven() && !RunningConfig.isRunningInDocker() && !RunningConfig.isRunningInAppveyor()) {
            return;
        } else if ((RunningConfig.isRunningInDocker() && !RunningConfig.isRunningInAppveyor()) || !RunningConfig.isRunningInMaven()) {
            consensusBroker.shutDownGracefully();
        }
    }
}
