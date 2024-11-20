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

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class KafkaConsumerPrivateGroup implements IKafkaComponent {

    private final ArrayList<String> ipAddresses;
    private final ConcurrentHashMap<String, Consumer<String, byte[]>> consumer_map;
    private final String current_ip;
    private final ExecutorService executorService;
    private int position;

    public KafkaConsumerPrivateGroup() {
        this.ipAddresses = new ArrayList<>();
        this.consumer_map = new ConcurrentHashMap<>();
        this.current_ip = IPFinder.getLocalIP();
        this.position = 0;
        this.executorService = null;
    }

    public KafkaConsumerPrivateGroup(ArrayList<String> ipAddresses, String current_ip) {
        this.ipAddresses = ipAddresses;
        this.consumer_map = new ConcurrentHashMap<>();
        this.current_ip = current_ip;
        this.position = this.ipAddresses.indexOf(current_ip);
        this.executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
    }

    @SneakyThrows
    @Override
    public void constructKafkaComponentType() {
        for (int i = 0; i < ipAddresses.size(); i++) {
            String ip = this.ipAddresses.get(i);
            if (ip.equals(current_ip) && !ip.equals("localhost"))
                continue;
            if (ip.equals("localhost")) {
                position = i;
            }
            Properties props = new Properties();
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, ip + ":" + KafkaConfiguration.KAFKA_PORT);
            props.put(ConsumerConfig.GROUP_ID_CONFIG, KafkaConfiguration.CONSUMER_PRIVATE_GROUP_ID + "-" + i + "-" + KafkaConfiguration.KAFKA_HOST);
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
            props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 1);
            props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 10);
            props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 60000);
            props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "600000");
            props.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, "1000");
            props.put(ConsumerConfig.METADATA_MAX_AGE_CONFIG, "1000");
            props.put(ConsumerConfig.RECONNECT_BACKOFF_MS_CONFIG, "500");
            props.put(ConsumerConfig.RECONNECT_BACKOFF_MAX_MS_CONFIG, "8000");
            props.put(ConsumerConfig.RETRY_BACKOFF_MS_CONFIG, "500");
            props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
            props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT");
            props.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
            props.put(SaslConfigs.SASL_JAAS_CONFIG, "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"" + "consumer" + "-" + position + "-" + current_ip + "\" password=\"consumer-secret\";");

//            System.out.println("1 "+props.getProperty(SaslConfigs.SASL_JAAS_CONFIG));
//            System.out.println("1 "+props.getProperty(ConsumerConfig.GROUP_ID_CONFIG));
//            System.out.println("1 "+props.getProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG));
//            System.out.println();
            //This is for maximizing the throughput of the consumer but for large messages
//            props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, "100000");
//            props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "500");
//            props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, "100");
            Consumer<String, byte[]> consumer = new KafkaConsumer<>(props);

            int finalI = i;
            Runnable task = () -> {
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
                            System.out.printf("Failed to fetch partition metadata. Attempt %d/%d%n", retryCount, maxRetries);
                            try {
                                Thread.sleep(2000); // Wait before retrying
                            } catch (InterruptedException ie) {
                                Thread.currentThread().interrupt();
                            }
                        }
                    }
                }
                consumer.subscribe(TopicFactory.getInstance().getCollectionTopicsNames());

                if (ip.equals("localhost")) {
                    if (finalI == 0) {
                        consumer_map.put(ip, consumer);
                    } else {
                        consumer_map.put(ip + String.valueOf(finalI), consumer);
                    }
                } else {
                    consumer_map.put(ip, consumer);
                }
                //Start Caching the messages
                consumer.poll(Duration.ofMillis(100));
                System.out.println("Consumer " + ip + " " + finalI + " started and subscribed to topics: " + TopicFactory.getInstance().getCollectionTopicsNames());
            };
            executorService.submit(task);
        }
        // Initiate shutdown
        executorService.shutdown();
        try {
            // Wait for tasks to complete or timeout
            if (!executorService.awaitTermination(KafkaConfiguration.PRIVATE_GROUP_METADATA_TIMEOUT, TimeUnit.MILLISECONDS)) {
                System.out.println("Timeout reached before tasks completed");
                executorService.shutdownNow();
            } else {
                System.out.println("All tasks completed");
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
        int g = 3;
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
                v.wakeup();
                v.close();
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

    public ConcurrentHashMap<String, Consumer<String, byte[]>> getConsumer_map() {
        return consumer_map;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }
}
