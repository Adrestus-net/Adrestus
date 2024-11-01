package io.Adrestus.network;

import io.Adrestus.config.KafkaConfiguration;
import io.Adrestus.streaming.*;
import lombok.SneakyThrows;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ConsensusBroker {
    private final ArrayList<String> ipAddresses;
    private final KafkaSmith kafkaManufactureSmith;
    private final ConcurrentHashMap<TopicType, TreeMap<Integer, Map<Integer, String>>> sequencedMap;
    private final String currentIP;
    private String leader_host;
    private int partition;

    public ConsensusBroker(ArrayList<String> ipAddresses, String leader_host, int partition) {
        this.currentIP = IPFinder.getLocalIP();
        this.ipAddresses = ipAddresses;
        this.ipAddresses.remove(this.currentIP);
        this.leader_host = leader_host;
        this.partition = partition;
        this.kafkaManufactureSmith = new KafkaManufactureSmith(ipAddresses);
        this.sequencedMap = new ConcurrentHashMap<TopicType, TreeMap<Integer, Map<Integer, String>>>();
        this.sequencedMap.put(TopicType.ANNOUNCE_PHASE, new TreeMap<>(Collections.reverseOrder()));
        this.sequencedMap.put(TopicType.PREPARE_PHASE, new TreeMap<>(Collections.reverseOrder()));
        this.sequencedMap.put(TopicType.COMMITTEE_PHASE, new TreeMap<>(Collections.reverseOrder()));
        this.sequencedMap.put(TopicType.DISPERSE_PHASE1, new TreeMap<>(Collections.reverseOrder()));
        this.sequencedMap.put(TopicType.DISPERSE_PHASE2, new TreeMap<>(Collections.reverseOrder()));
    }

    public void initializeKafkaKingdom() {
        this.kafkaManufactureSmith.manufactureKafkaComponent(KafkaKingdomType.ZOOKEEPER);
        this.kafkaManufactureSmith.manufactureKafkaComponent(KafkaKingdomType.BROKER);
        this.kafkaManufactureSmith.manufactureKafkaComponent(KafkaKingdomType.TOPIC_CREATOR);
        this.kafkaManufactureSmith.manufactureKafkaComponent(KafkaKingdomType.PRODUCER);
        this.kafkaManufactureSmith.manufactureKafkaComponent(KafkaKingdomType.CONSUMER_PRIVATE);
        if(!currentIP.equals(leader_host)) {
            this.kafkaManufactureSmith.updateLeaderHost(KafkaKingdomType.CONSUMER_SAME, this.ipAddresses, this.leader_host, this.partition, false);
        }
    }


    public void updateLeaderHost(String leader_host, int partition) {
        this.kafkaManufactureSmith.updateLeaderHost(KafkaKingdomType.CONSUMER_SAME, this.ipAddresses, leader_host, partition, true);
    }

    public void produceMessage(TopicType topic, String key, String value) {
        KafkaProducer producer = this.kafkaManufactureSmith.getKafkaComponent(KafkaKingdomType.PRODUCER);
        producer.produceMessage(topic.name(), key, value);

    }

    public void produceMessage(TopicType topic, int partition, String key, String value) {
        KafkaProducer producer = this.kafkaManufactureSmith.getKafkaComponent(KafkaKingdomType.PRODUCER);
        producer.produceMessage(topic.name(), partition, key, value);

    }

    @SneakyThrows
    public List<String> receiveMessageFromValidators(TopicType topic, String key) {
        KafkaConsumerPrivateGroup leaderConsumeData = this.kafkaManufactureSmith.getKafkaComponent(KafkaKingdomType.CONSUMER_PRIVATE);
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
        List<Consumer<String, String>> consumers = leaderConsumeData.receiveAllBrokerConsumersExceptLeader();
        for (int i = 0; i < consumers.size(); i++) {
            int finalI = i;
            Runnable task = () -> {
                while (true) {
                    Consumer<String, String> consumer = consumers.get(finalI);
                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(KafkaConfiguration.RECEIVE_TIMEOUT));
                    for (ConsumerRecord<String, String> record : records) {
                        if (!this.sequencedMap.get(topic).containsKey(Integer.parseInt(record.key()))) {
                            this.sequencedMap.get(topic).put(Integer.parseInt(record.key()), new HashMap<>());
                        }
                        this.sequencedMap.get(topic).get(Integer.parseInt(record.key())).put(finalI, record.value());
                        if (record.key().equals(key)) {
                            break;
                        }
                    }
                }
            };
            executorService.submit(task);
        }
        executorService.shutdown();
        executorService.awaitTermination(KafkaConfiguration.EXECUTOR_TIMEOUT, TimeUnit.MILLISECONDS);
        executorService.shutdownNow();
        executorService.close();
        return this.sequencedMap.get(topic).firstEntry().getValue().values().stream().collect(Collectors.toList());
    }

    @SneakyThrows
    public Optional<String> receiveMessageFromLeader(TopicType topic, String key, String leader_host) {
        KafkaConsumerPrivateGroup leaderConsumeData = this.kafkaManufactureSmith.getKafkaComponent(KafkaKingdomType.CONSUMER_PRIVATE);
        ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
        Consumer<String, String> consumer = leaderConsumeData.receiveLeaderConsumer(leader_host);
        Runnable task = () -> {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(KafkaConfiguration.RECEIVE_TIMEOUT));
                for (ConsumerRecord<String, String> record : records) {
                    if (!this.sequencedMap.get(topic).containsKey(Integer.parseInt(record.key()))) {
                        this.sequencedMap.get(topic).put(Integer.parseInt(record.key()), new HashMap<>());
                    }
                    this.sequencedMap.get(topic).get(Integer.parseInt(record.key())).put(0, record.value());
                    if (record.key().equals(key)) {
                        break;
                    }
                }
            }
        };
        executorService.submit(task);
        executorService.shutdown();
        executorService.awaitTermination(KafkaConfiguration.EXECUTOR_TIMEOUT, TimeUnit.MILLISECONDS);
        executorService.shutdownNow();
        executorService.close();
        return this.sequencedMap.get(topic).firstEntry().getValue().values().stream().findFirst();
    }

    @SneakyThrows
    public Optional<String> receiveDisperseMessageFromLeader(TopicType topic, String key) {
        KafkaConsumerSameGroup leaderSameConsumeData = this.kafkaManufactureSmith.getKafkaComponent(KafkaKingdomType.CONSUMER_SAME);
        ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
        Consumer<String, String> consumer = leaderSameConsumeData.getConsumer();
        Runnable task = () -> {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(KafkaConfiguration.RECEIVE_TIMEOUT));
                for (ConsumerRecord<String, String> record : records) {
                    if (!this.sequencedMap.get(topic).containsKey(Integer.parseInt(record.key()))) {
                        this.sequencedMap.get(topic).put(Integer.parseInt(record.key()), new HashMap<>());
                    }
                    this.sequencedMap.get(topic).get(Integer.parseInt(record.key())).put(0, record.value());
                    if (record.key().equals(key)) {
                        break;
                    }
                }
            }
        };
        executorService.submit(task);
        executorService.shutdown();
        executorService.awaitTermination(KafkaConfiguration.EXECUTOR_TIMEOUT, TimeUnit.MILLISECONDS);
        executorService.shutdownNow();
        executorService.close();
        return this.sequencedMap.get(topic).firstEntry().getValue().values().stream().findFirst();
    }

    public void clearMap() {
        this.sequencedMap.clear();
    }

    public void close() {
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