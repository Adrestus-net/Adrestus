package io.Adrestus.network;

import io.Adrestus.config.KafkaConfiguration;
import lombok.SneakyThrows;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class KafkaConsumerPrivateGroup implements IKafkaComponent {

    private ArrayList<String> ipAddresses;
    private HashMap<String, Consumer<String, byte[]>> consumer_map;
    private String current_ip;

    public KafkaConsumerPrivateGroup() {
        this.ipAddresses = new ArrayList<>();
        this.consumer_map = new HashMap<>();
        this.current_ip = IPFinder.getLocalIP();
    }

    public KafkaConsumerPrivateGroup(ArrayList<String> ipAddresses, String current_ip) {
        this.ipAddresses = ipAddresses;
        this.consumer_map = new HashMap<>();
        this.current_ip = current_ip;
    }

    @SneakyThrows
    @Override
    public void constructKafkaComponentType() {
        int position = ipAddresses.indexOf(current_ip);
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
            props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 4098);
            props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 10);
            //props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 1);
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
            consumer.subscribe(TopicFactory.getInstance().getCollectionTopicsNames());
            if (consumer_map.containsKey(ip)) {
                consumer_map.put(ip + String.valueOf(i), consumer);
            } else {
                consumer_map.put(ip, consumer);
            }
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

    public HashMap<String, Consumer<String, byte[]>> getConsumer_map() {
        return consumer_map;
    }

    public void setConsumer_map(HashMap<String, Consumer<String, byte[]>> consumer_map) {
        this.consumer_map = consumer_map;
    }
}
