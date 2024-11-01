package io.Adrestus.streaming;

import io.Adrestus.config.KafkaConfiguration;
import io.Adrestus.network.ConsensusServer;
import io.Adrestus.network.IPFinder;
import lombok.SneakyThrows;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class KafkaConsumerPrivateGroup implements IKafkaComponent {

    private ArrayList<String> ipAddresses;
    private HashMap<String, Consumer<String, String>> consumer_map;

    public KafkaConsumerPrivateGroup() {
        this.ipAddresses = new ArrayList<>();
        this.consumer_map = new HashMap<>();
    }

    public KafkaConsumerPrivateGroup(ArrayList<String> ipAddresses) {
        this.ipAddresses = ipAddresses;
        this.consumer_map = new HashMap<>();
    }

    @SneakyThrows
    @Override
    public void constructKafkaComponentType() {
        for (int i = 0; i < ipAddresses.size(); i++) {
            Properties props = new Properties();
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, this.ipAddresses.get(i) + ":" + KafkaConfiguration.KAFKA_PORT);
            props.put(ConsumerConfig.GROUP_ID_CONFIG, KafkaConfiguration.CONSUMER_PRIVATE_GROUP_ID + "-" + i + "-" + KafkaConfiguration.KAFKA_HOST);
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
            props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 4098);
            props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 10);
            props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 1);
            props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

            //This is for maximizing the throughput of the consumer but for large messages
//            props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, "100000");
//            props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "500");
//            props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, "100");
            Consumer<String, String> consumer = new KafkaConsumer<>(props);
            consumer.subscribe(TopicFactory.getInstance().getCollectionTopicsNames());
            if (consumer_map.containsKey(ipAddresses.get(i))) {
                consumer_map.put(ipAddresses.get(i) + String.valueOf(i), consumer);
            } else {
                consumer_map.put(ipAddresses.get(i), consumer);
            }
        }
    }

    private String getConnecntionString() {
       StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < ipAddresses.size(); i++) {
            stringBuilder.append(this.ipAddresses.get(i) + ":" + KafkaConfiguration.KAFKA_PORT+ ",");
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        return stringBuilder.toString();
    }
    public List<Consumer<String, String>> receiveAllBrokerConsumersExceptLeader() {
        return consumer_map.entrySet().stream()
                .map(HashMap.Entry::getValue)
                .collect(Collectors.toList());
    }

    public Consumer<String, String> receiveLeaderConsumer(String leader_ip) {
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
            this.ipAddresses = null;
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

    public void setIpAddresses(ArrayList<String> ipAddresses) {
        this.ipAddresses = ipAddresses;
        this.ipAddresses.remove(IPFinder.getLocalIP());
    }

    public HashMap<String, Consumer<String, String>> getConsumer_map() {
        return consumer_map;
    }

    public void setConsumer_map(HashMap<String, Consumer<String, String>> consumer_map) {
        this.consumer_map = consumer_map;
    }
}
