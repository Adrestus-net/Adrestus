package io.Adrestus.network;

import io.Adrestus.config.KafkaConfiguration;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.RoundRobinAssignor;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class KafkaConsumerSameGroup implements IKafkaComponent, Cloneable {
    private static final Logger LOG = LoggerFactory.getLogger(KafkaConsumerSameGroup.class);

    private final ConcurrentHashMap<String, Consumer<String, byte[]>> consumer_map;
    private final ArrayList<String> ipAddresses;
    private final String currentIP;
    private final int partition;

    public KafkaConsumerSameGroup() {
        this.ipAddresses = new ArrayList<>();
        this.consumer_map = new ConcurrentHashMap<>();
        this.currentIP = "";
        this.partition = 0;
    }

    public KafkaConsumerSameGroup(ArrayList<String> ipAddresses, String currentIP, int partition) {
        this.ipAddresses = ipAddresses;
        this.consumer_map = new ConcurrentHashMap<>();
        this.partition = partition;
        this.currentIP = currentIP;
    }

    @Override
    public void constructKafkaComponentType() {
        for (int i = 0; i < ipAddresses.size(); i++) {
            String ip = this.ipAddresses.get(i);
            if (ip.equals(currentIP) && !ip.equals("127.0.0.1"))
                continue;
            Properties props = new Properties();
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, ip + ":" + KafkaConfiguration.KAFKA_PORT);
            props.put(ConsumerConfig.GROUP_ID_CONFIG, KafkaConfiguration.CONSUMER_SAME_GROUP_ID);
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
            props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, "7000");
            props.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, "13107200");
            props.put(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, "536870912");
            props.put(ConsumerConfig.RECEIVE_BUFFER_CONFIG, "-1");
            props.put(ConsumerConfig.SEND_BUFFER_CONFIG, "-1");
            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
            props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, "1");
            props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, "10");
            props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "60000");
            props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "600000");
            props.put(ConsumerConfig.AUTO_INCLUDE_JMX_REPORTER_CONFIG, "false");
            props.put(ConsumerConfig.ENABLE_METRICS_PUSH_CONFIG, "FALSE");
            props.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, "1000");
            //props.put(ConsumerConfig.METADATA_MAX_AGE_CONFIG, "1000");
            props.put(ConsumerConfig.RETRY_BACKOFF_MS_CONFIG, "300");
            props.put(ConsumerConfig.RETRY_BACKOFF_MAX_MS_CONFIG, "7000");
            props.put(ConsumerConfig.RECONNECT_BACKOFF_MS_CONFIG, "300");
            props.put(ConsumerConfig.RECONNECT_BACKOFF_MAX_MS_CONFIG, "7000");
            props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
            props.put(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG, RoundRobinAssignor.class.getName());
            props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT");
            props.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
            props.put(SaslConfigs.SASL_JAAS_CONFIG, "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"" + "consumer" + "-" + this.partition + "-" + this.currentIP + "\" password=\"consumer-secret\";");

            Consumer<String, byte[]> consumer = new KafkaConsumer<>(props);
            TopicPartition partition = new TopicPartition(TopicType.DISPERSE_PHASE1.name(), this.partition);
            consumer.partitionsFor(TopicType.DISPERSE_PHASE1.name());
            consumer.assign(Collections.singleton(partition));
            consumer.seekToBeginning(Collections.singleton(partition));
            if (ip.equals("127.0.0.1")) {
                if (i == 0) {
                    consumer_map.put(ip, consumer);
                } else {
                    consumer_map.put(ip + String.valueOf(i), consumer);
                }
            } else {
                consumer_map.put(ip, consumer);
            }
        }
        LOG.info("Node " + this.partition + " " + "Subscribing to partition: " + partition);
    }


    @Override
    public IKafkaComponent getKafkaKingdomType() {
        return this;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public Consumer<String, byte[]> receiveLeaderConsumer(String leader_ip) {
        return consumer_map.entrySet().stream()
                .filter(e -> e.getKey().equals(leader_ip))
                .map(HashMap.Entry::getValue)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Consumer not found"));
    }

    public Optional<Integer> getPositionOfElement(String host) {
        List<String> keys = new ArrayList<>(consumer_map.keySet());
        int position = keys.indexOf(host);
        keys.clear();
        return position >= 0 ? Optional.of(position) : Optional.empty();
    }

    @Override
    public void Shutdown() {
        if (ipAddresses != null) {
            this.ipAddresses.clear();
        }
        this.consumer_map.forEach((k, v) -> {
            try {
                v.wakeup();
                v.close();
            } catch (Exception e) {
            }
        });
        this.consumer_map.clear();
    }

    public ArrayList<String> getIpAddresses() {
        return ipAddresses;
    }

    public ConcurrentHashMap<String, Consumer<String, byte[]>> getConsumer_map() {
        return consumer_map;
    }

    public int getPartition() {
        return partition;
    }

    public String getCurrentIP() {
        return currentIP;
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        KafkaConsumerSameGroup that = (KafkaConsumerSameGroup) o;
        return partition == that.partition && Objects.equals(ipAddresses, that.ipAddresses) && Objects.equals(currentIP, that.currentIP) && Objects.equals(consumer_map, that.consumer_map);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ipAddresses, currentIP, partition, consumer_map);
    }

    @Override
    public String toString() {
        return "KafkaConsumerSameGroup{" +
                "ipAddresses=" + ipAddresses +
                ", currentIP='" + currentIP + '\'' +
                ", partition=" + partition +
                ", consumer_map=" + consumer_map +
                '}';
    }
}
