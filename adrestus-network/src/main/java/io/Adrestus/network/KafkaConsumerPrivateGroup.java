package io.Adrestus.network;

import io.Adrestus.config.KafkaConfiguration;
import lombok.SneakyThrows;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class KafkaConsumerPrivateGroup implements IKafkaComponent {
    private static final Logger LOG = LoggerFactory.getLogger(KafkaConsumerPrivateGroup.class);

    private final ArrayList<String> ipAddresses;
    private final LinkedHashMap<String, Consumer<String, byte[]>> consumer_map;
    private final String current_ip;
    private int position;

    public KafkaConsumerPrivateGroup() {
        this.ipAddresses = new ArrayList<>();
        this.consumer_map = new LinkedHashMap<>();
        this.current_ip = IPFinder.getLocalIP();
        this.position = 0;
    }

    public KafkaConsumerPrivateGroup(ArrayList<String> ipAddresses, String current_ip) {
        this.ipAddresses = ipAddresses;
        this.consumer_map = new LinkedHashMap<>();
        this.current_ip = current_ip;
        this.position = this.ipAddresses.indexOf(current_ip);
    }

    @SneakyThrows
    @Override
    public void constructKafkaComponentType() {
        for (int i = 0; i < ipAddresses.size(); i++) {
            String ip = this.ipAddresses.get(i);
            String groupID;
            if (ip.equals(current_ip) && !ip.equals("127.0.0.1"))
                continue;
            if (ip.equals("127.0.0.1")) {
                position = i;
                groupID = KafkaConfiguration.CONSUMER_PRIVATE_GROUP_ID + "-" + position + "-" + ip + "-" + KafkaConfiguration.KAFKA_HOST;
            } else
                groupID = KafkaConfiguration.CONSUMER_PRIVATE_GROUP_ID + "-" + ip + "-" + KafkaConfiguration.KAFKA_HOST;
            Properties props = new Properties();
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, ip + ":" + KafkaConfiguration.KAFKA_PORT);
            props.put(ConsumerConfig.GROUP_ID_CONFIG, groupID);
            props.put(ConsumerConfig.GROUP_INSTANCE_ID_CONFIG, groupID);
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
            props.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, "13107200");
            props.put(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, "536870912");
            props.put(ConsumerConfig.RECEIVE_BUFFER_CONFIG, "-1");
            props.put(ConsumerConfig.SEND_BUFFER_CONFIG, "-1");
            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "none");
            props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
            props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, "1");
            props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, "100");
            props.put(ConsumerConfig.METADATA_MAX_AGE_CONFIG, "1000");
            props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "100");
            props.put(ConsumerConfig.AUTO_INCLUDE_JMX_REPORTER_CONFIG, "false");
            props.put(ConsumerConfig.ENABLE_METRICS_PUSH_CONFIG, "FALSE");
            props.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, "1000");
            props.put(ConsumerConfig.RETRY_BACKOFF_MS_CONFIG, "300");
            props.put(ConsumerConfig.RETRY_BACKOFF_MAX_MS_CONFIG, "7000");
            props.put(ConsumerConfig.RECONNECT_BACKOFF_MS_CONFIG, "300");
            props.put(ConsumerConfig.RECONNECT_BACKOFF_MAX_MS_CONFIG, "7000");
            props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "9000");
            props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, "3000");
            props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "5000");
            props.put(ConsumerConfig.SOCKET_CONNECTION_SETUP_TIMEOUT_MS_CONFIG, "20000");
            props.put(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG, "org.apache.kafka.clients.consumer.StickyAssignor");
            props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT");
            props.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
            props.put(SaslConfigs.SASL_JAAS_CONFIG, "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"" + "consumer" + "-" + position + "-" + current_ip + "\" password=\"consumer-secret\";");

            Consumer<String, byte[]> consumer = new KafkaConsumer<>(props);

            for (String topic : TopicFactory.getInstance().getCollectionTopicsNames()) {
                int maxRetries = 5;
                int retryCount = 0;
                boolean success = false;
                while (retryCount < maxRetries && !success) {
                    try {
                        List<PartitionInfo> partitions = consumer.partitionsFor(topic);
                        if (partitions != null) {
                            success = true;
                        }
                    } catch (Exception e) {
                        retryCount++;
                        //LOG.info("Failed to fetch partition metadata. Attempt %d/%d%n", retryCount, maxRetries);
                        try {
                            Thread.sleep(1000); // Wait before retrying
                        } catch (InterruptedException ie) {
                        }
                    }
                }
            }

            consumer.assign(TopicFactory.getInstance().getCollectionTopicPartitions());
            consumer.seekToBeginning(TopicFactory.getInstance().getCollectionTopicPartitions());

            if (ip.equals("127.0.0.1")) {
                if (i == 0) {
                    consumer_map.put(ip, consumer);
                } else {
                    consumer_map.put(ip + String.valueOf(i), consumer);
                }
            } else {
                consumer_map.put(ip, consumer);
            }
            //Start Caching the messages
            // Wait for partitions to be assigned
            while (consumer.assignment().isEmpty()) {
                consumer.poll(Duration.ofMillis(100)); // Trigger partition assignment
            }
            LOG.info("Consumer " + ip + " " + i + " started and subscribed to topics: " + TopicFactory.getInstance().getCollectionTopicsNames());
        }
    }

    private String getConnecntionString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < ipAddresses.size(); i++) {
            stringBuilder.append(this.ipAddresses.get(i) + ":" + KafkaConfiguration.KAFKA_PORT + ",");
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        return stringBuilder.toString();
    }

    public List<Consumer<String, byte[]>> receiveAllBrokerConsumersExceptLeader(String leader_ip) {
        return consumer_map.entrySet().stream()
                .filter(e -> !e.getKey().equals(leader_ip))
                .map(HashMap.Entry::getValue)
                .collect(Collectors.toList());
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

    public List<String> getMapKeys() {
        return new ArrayList<>(consumer_map.keySet());
    }

    @Override
    public IKafkaComponent getKafkaKingdomType() {
        return this;
    }

    @Override
    public void Shutdown() {
        if (ipAddresses != null) {
            this.ipAddresses.clear();
        }
        if (consumer_map != null) {
            this.consumer_map.forEach((k, v) -> {
                try {
                    v.wakeup();
                    v.close();
                } catch (Exception e) {
                }
            });
            this.consumer_map.clear();
        }
    }

    @Override
    public String toString() {
        return "KafkaConsumerPrivateGroup{}";
    }

    public ArrayList<String> getIpAddresses() {
        return ipAddresses;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getCurrent_ip() {
        return current_ip;
    }

    public LinkedHashMap<String, Consumer<String, byte[]>> getConsumer_map() {
        return consumer_map;
    }
}
