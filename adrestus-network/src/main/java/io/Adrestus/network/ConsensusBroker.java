package io.Adrestus.network;

import com.google.common.reflect.TypeToken;
import io.Adrestus.config.KafkaConfiguration;
import io.Adrestus.util.SerializationUtil;
import lombok.SneakyThrows;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class ConsensusBroker {
    private final ArrayList<String> prototypeIPAddresses;
    private final SerializationUtil<ArrayList<byte[]>> serenc_erasure;
    private final ArrayList<String> ipAddresses;
    private final KafkaSmith kafkaManufactureSmith;
    private final ConcurrentHashMap<TopicType, TreeMap<Integer, Map<Integer, byte[]>>> sequencedMap;
    private final String currentIP;
    private final int numThreads;
    private String leader_host;
    private int partition;

    public ConsensusBroker(ArrayList<String> ipAddresses, String leader_host, int partition) {
        this.serenc_erasure = new SerializationUtil<ArrayList<byte[]>>(new TypeToken<List<byte[]>>() {
        }.getType());
        this.numThreads = Runtime.getRuntime().availableProcessors() * 2;
        this.currentIP = IPFinder.getLocalIP();
        this.ipAddresses = ipAddresses;
        this.prototypeIPAddresses = new ArrayList<>(this.ipAddresses);
        this.ipAddresses.remove(this.currentIP);
        this.leader_host = leader_host;
        this.partition = partition;
        this.kafkaManufactureSmith = new KafkaManufactureSmith(ipAddresses);
        this.kafkaManufactureSmith.updateACLKafkaList(this.prototypeIPAddresses);
        this.sequencedMap = new ConcurrentHashMap<TopicType, TreeMap<Integer, Map<Integer, byte[]>>>();
        this.sequencedMap.put(TopicType.ANNOUNCE_PHASE, new TreeMap<>(Collections.reverseOrder()));
        this.sequencedMap.put(TopicType.PREPARE_PHASE, new TreeMap<>(Collections.reverseOrder()));
        this.sequencedMap.put(TopicType.COMMITTEE_PHASE, new TreeMap<>(Collections.reverseOrder()));
        this.sequencedMap.put(TopicType.DISPERSE_PHASE1, new TreeMap<>(Collections.reverseOrder()));
        this.sequencedMap.put(TopicType.DISPERSE_PHASE2, new TreeMap<>(Collections.reverseOrder()));
    }

    @SneakyThrows
    public void initializeKafkaKingdom() {
        this.kafkaManufactureSmith.manufactureKafkaComponent(KafkaKingdomType.ZOOKEEPER);
        this.kafkaManufactureSmith.manufactureKafkaComponent(KafkaKingdomType.BROKER);
        this.kafkaManufactureSmith.manufactureKafkaComponent(KafkaKingdomType.TOPIC_CREATOR);
        this.kafkaManufactureSmith.manufactureKafkaComponent(KafkaKingdomType.PRODUCER);
        this.kafkaManufactureSmith.manufactureKafkaComponent(KafkaKingdomType.CONSUMER_PRIVATE);
        if (!currentIP.equals(leader_host)) {
            this.kafkaManufactureSmith.updateLeaderHost(KafkaKingdomType.CONSUMER_SAME, this.ipAddresses, this.leader_host, this.partition, false);
        }
        this.setupChannels();
    }


    private void setupChannels() throws InterruptedException {
        if (currentIP.equals(leader_host)) {
            for (int i = 0; i < this.prototypeIPAddresses.size(); i++) {
                this.produceMessage(TopicType.DISPERSE_PHASE1, i, String.valueOf(i), "0".getBytes(StandardCharsets.UTF_8));
            }
            this.flush();

            this.produceMessage(TopicType.ANNOUNCE_PHASE, "0", "0".getBytes(StandardCharsets.UTF_8));
            this.receiveMessageFromValidators(TopicType.ANNOUNCE_PHASE, "0");
            System.out.println("1");
            this.produceMessage(TopicType.PREPARE_PHASE, "0", "0".getBytes());
            this.receiveMessageFromValidators(TopicType.PREPARE_PHASE, "0");
            System.out.println("2");
            this.produceMessage(TopicType.COMMITTEE_PHASE, "0", "0".getBytes(StandardCharsets.UTF_8));
            this.receiveMessageFromValidators(TopicType.COMMITTEE_PHASE, "0");
            System.out.println("3");
            this.produceMessage(TopicType.DISPERSE_PHASE2, "0", "0".getBytes(StandardCharsets.UTF_8));
            this.receiveMessageFromValidators(TopicType.DISPERSE_PHASE2, "0");
            System.out.println("4");
        } else {
            this.receiveDisperseMessageFromLeader(TopicType.DISPERSE_PHASE1, String.valueOf(partition));
            this.receiveMessageFromLeader(TopicType.ANNOUNCE_PHASE, "0");
            this.produceMessage(TopicType.ANNOUNCE_PHASE, "0", "0".getBytes(StandardCharsets.UTF_8));
            this.receiveMessageFromLeader(TopicType.PREPARE_PHASE, "0");
            this.produceMessage(TopicType.PREPARE_PHASE, "0", "0".getBytes(StandardCharsets.UTF_8));
            this.receiveMessageFromLeader(TopicType.COMMITTEE_PHASE, "0");
            this.produceMessage(TopicType.COMMITTEE_PHASE, "0", "0".getBytes(StandardCharsets.UTF_8));
            this.receiveMessageFromLeader(TopicType.DISPERSE_PHASE2, "0");
            this.produceMessage(TopicType.DISPERSE_PHASE2, "0", "0".getBytes(StandardCharsets.UTF_8));
        }
    }

    public void updateLeaderHost(String leader_host, int partition) {
        this.kafkaManufactureSmith.updateLeaderHost(KafkaKingdomType.CONSUMER_SAME, this.ipAddresses, leader_host, partition, true);
    }

    public void distributeDisperseMessageFromLeader(ArrayList<ArrayList<byte[]>> data, String key) {
        if (data.size() != this.prototypeIPAddresses.size() - 1)
            throw new IllegalArgumentException("DisperseMessage Invalid data size");


        int j = 0;
        for (int i = 0; i < this.prototypeIPAddresses.size(); i++) {
            if (this.prototypeIPAddresses.get(i).equals(this.currentIP))
                continue;
            ArrayList<byte[]> toSend = data.get(j);
            int sum = toSend.parallelStream().mapToInt(byteArray -> byteArray.length).sum() + 1024 * 6;
            this.produceMessage(TopicType.DISPERSE_PHASE1, i, key + i, this.serenc_erasure.encode(toSend, sum));
            j++;
        }

        this.flush();
    }

    public void distributeDisperseMessageToValidators(ArrayList<byte[]> data, String key) {
        if (data.isEmpty())
            throw new IllegalArgumentException("DisperseMessageToValidators Invalid data size");

        this.produceMessage(TopicType.DISPERSE_PHASE2, key, this.serenc_erasure.encode(data, data.parallelStream().mapToInt(byteArray -> byteArray.length).sum() + 1024 * 6));
        this.flush();
    }

    public ArrayList<ArrayList<byte[]>> retrieveDisperseMessageFromValidatorsAndConcatResponse(ArrayList<byte[]> leader_data, String key) {
        List<byte[]> response = this.receiveMessageFromValidators(TopicType.DISPERSE_PHASE2, key);
        if (response.isEmpty())
            throw new IllegalArgumentException("DisperseMessageFromValidators Invalid data size");

        ArrayList<ArrayList<byte[]>> concatResponse = new ArrayList<>();
        response.forEach(val -> concatResponse.add(new ArrayList<>(this.serenc_erasure.decode(val))));
        concatResponse.addFirst(leader_data);
        return concatResponse;
    }

    public void produceMessage(TopicType topic, String key, byte[] data) {
        KafkaProducer producer = this.kafkaManufactureSmith.getKafkaComponent(KafkaKingdomType.PRODUCER);
        producer.produceMessage(topic.name(), key, data);

    }

    public void produceMessage(TopicType topic, int partition, String key, byte[] data) {
        KafkaProducer producer = this.kafkaManufactureSmith.getKafkaComponent(KafkaKingdomType.PRODUCER);
        producer.produceMessage(topic.name(), partition, key, data);

    }

    public void flush() {
        KafkaProducer producer = this.kafkaManufactureSmith.getKafkaComponent(KafkaKingdomType.PRODUCER);
        producer.getProducer().flush();
    }

    @SneakyThrows
    public List<byte[]> receiveMessageFromValidators(TopicType topic, String key) {
        KafkaConsumerPrivateGroup leaderConsumeData = this.kafkaManufactureSmith.getKafkaComponent(KafkaKingdomType.CONSUMER_PRIVATE);
        List<Consumer<String, byte[]>> consumers = leaderConsumeData.receiveAllBrokerConsumersExceptLeader(leader_host);
        ExecutorService executorService = Executors.newFixedThreadPool(this.numThreads);
        CountDownLatch await_latch = new CountDownLatch(consumers.size());
        for (int i = 0; i < consumers.size(); i++) {
            int finalI = i;
            Runnable task = () -> {
                int timeout = 0;
                boolean flag = false;
                while (timeout < KafkaConfiguration.EXECUTOR_TIMEOUT) {
                    Consumer<String, byte[]> consumer = consumers.get(finalI);
                    ConsumerRecords<String, byte[]> records = consumer.poll(Duration.ofMillis(KafkaConfiguration.RECEIVE_TIMEOUT));
                    for (ConsumerRecord<String, byte[]> record : records) {
                        TopicType current = TopicType.valueOf(record.topic());
                        if (!this.sequencedMap.get(current).containsKey(Integer.parseInt(record.key()))) {
                            this.sequencedMap.get(current).put(Integer.parseInt(record.key()), new HashMap<>());
                        }
                        this.sequencedMap.get(current).get(Integer.parseInt(record.key())).put(finalI, record.value());
                        if (record.key().equals(key) && record.topic().equals(topic.name())) {
                            flag = true;
                            await_latch.countDown();
                            break;
                        }
                    }
                    timeout++;
                }
                if (!flag)
                    await_latch.countDown();
            };
            executorService.submit(task);
        }
        await_latch.await();
        executorService.shutdownNow();
        executorService.close();
        return this.sequencedMap.get(topic).firstEntry().getValue().values().stream().collect(Collectors.toList());
    }

    @SneakyThrows
    public Optional<byte[]> receiveMessageFromLeader(TopicType topic, String key) {
        KafkaConsumerPrivateGroup leaderConsumeData = this.kafkaManufactureSmith.getKafkaComponent(KafkaKingdomType.CONSUMER_PRIVATE);
        ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
        Consumer<String, byte[]> consumer = leaderConsumeData.receiveLeaderConsumer(leader_host);
        CountDownLatch await_latch = new CountDownLatch(1);
        Runnable task = () -> {
            int timeout = 0;
            boolean flag = false;
            while (timeout < KafkaConfiguration.EXECUTOR_TIMEOUT) {
                ConsumerRecords<String, byte[]> records = consumer.poll(Duration.ofMillis(KafkaConfiguration.RECEIVE_TIMEOUT));
                for (ConsumerRecord<String, byte[]> record : records) {
                    TopicType current = TopicType.valueOf(record.topic());
                    if (!this.sequencedMap.get(current).containsKey(Integer.parseInt(record.key()))) {
                        this.sequencedMap.get(current).put(Integer.parseInt(record.key()), new HashMap<>());
                    }
                    this.sequencedMap.get(current).get(Integer.parseInt(record.key())).put(0, record.value());
                    if (record.key().equals(key) && record.topic().equals(topic.name())) {
                        flag = true;
                        await_latch.countDown();
                        break;
                    }
                }
                timeout++;
            }
            if (!flag)
                await_latch.countDown();
        };
        executorService.submit(task);
        await_latch.await();
        executorService.shutdownNow();
        executorService.close();
        return this.sequencedMap.get(topic).firstEntry().getValue().values().stream().findFirst();
    }

    @SneakyThrows
    public ArrayList<byte[]> receiveDisperseHandledMessageFromLeader(TopicType topic, String key) {
        Optional<byte[]> message = this.receiveDisperseMessageFromLeader(topic, key);
        if (message.isEmpty())
            throw new IllegalArgumentException("DisperseMessage not received correctly form leader aborting");

        return new ArrayList<>(this.serenc_erasure.decode(message.get()));
    }

    @SneakyThrows
    public Optional<byte[]> receiveDisperseMessageFromLeader(TopicType topic, String key) {
        KafkaConsumerSameGroup leaderSameConsumeData = this.kafkaManufactureSmith.getKafkaComponent(KafkaKingdomType.CONSUMER_SAME);
        ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
        Consumer<String, byte[]> consumer = leaderSameConsumeData.getConsumer();
        CountDownLatch await_latch = new CountDownLatch(1);
        Runnable task = () -> {
            int timeout = 0;
            boolean flag = false;
            while (timeout < KafkaConfiguration.EXECUTOR_TIMEOUT) {
                ConsumerRecords<String, byte[]> records = consumer.poll(Duration.ofMillis(KafkaConfiguration.RECEIVE_TIMEOUT));
                for (ConsumerRecord<String, byte[]> record : records) {
                    TopicType current = TopicType.valueOf(record.topic());
                    if (!this.sequencedMap.get(current).containsKey(Integer.parseInt(record.key()))) {
                        this.sequencedMap.get(current).put(Integer.parseInt(record.key()), new HashMap<>());
                    }
                    this.sequencedMap.get(current).get(Integer.parseInt(record.key())).put(0, record.value());
                    if (record.key().equals(key) && record.topic().equals(topic.name())) {
                        flag = true;
                        await_latch.countDown();
                        break;
                    }
                }
                timeout++;
            }
            if (!flag)
                await_latch.countDown();
        };
        executorService.submit(task);
        await_latch.await();
        executorService.shutdownNow();
        executorService.close();
        return this.sequencedMap.get(topic).firstEntry().getValue().values().stream().findFirst();
    }

    public void clearMap() {
        this.sequencedMap.clear();
    }

    public void shutDownGracefully() {
        this.ipAddresses.clear();
        this.leader_host = null;
        this.partition = 0;
        this.kafkaManufactureSmith.shutDownGracefully();
    }

    public ArrayList<String> getIpAddresses() {
        return ipAddresses;
    }

    public KafkaSmith getKafkaManufactureSmith() {
        return kafkaManufactureSmith;
    }

    public String getLeader_host() {
        return leader_host;
    }

    public void setLeader_host(String leader_host) {
        this.leader_host = leader_host;
    }

    public int getPartition() {
        return partition;
    }

    public void setPartition(int partition) {
        this.partition = partition;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConsensusBroker that = (ConsensusBroker) o;
        return partition == that.partition && Objects.equals(ipAddresses, that.ipAddresses) && Objects.equals(kafkaManufactureSmith, that.kafkaManufactureSmith) && Objects.equals(leader_host, that.leader_host);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ipAddresses, kafkaManufactureSmith, leader_host, partition);
    }

    @Override
    public String toString() {
        return "ConsensusBroker{" +
                "ipAddresses=" + ipAddresses +
                ", kafkaManufactureSmith=" + kafkaManufactureSmith +
                ", leader_host='" + leader_host + '\'' +
                ", partition=" + partition +
                '}';
    }
}

