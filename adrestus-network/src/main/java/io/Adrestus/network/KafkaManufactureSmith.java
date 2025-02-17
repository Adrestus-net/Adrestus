package io.Adrestus.network;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

public class KafkaManufactureSmith implements KafkaSmith {
    private final Map<KafkaKingdomType, IKafkaComponent> map;
    private final ArrayList<String> ipAddresses;
    private final String leader_host;
    private final String currentIP;
    private final int partition;


    public KafkaManufactureSmith(ArrayList<String> ipAddresses, String leader_host, String currentIP, int partition) {
        this.ipAddresses = ipAddresses;
        this.leader_host = leader_host;
        this.currentIP = currentIP;
        this.partition = partition;
        this.map = new EnumMap<>(KafkaKingdomType.class);
        Arrays.stream(KafkaKingdomType.values()).forEach(type -> Init(type, this.ipAddresses, this.leader_host, this.currentIP, this.partition));
    }


    private void Init(KafkaKingdomType type, ArrayList<String> ipAddresses, String leader_host, String currentIP, int partition) {
        switch (type) {
            case PRODUCER:
                map.put(type, new KafkaProducer());
                break;
            case ZOOKEEPER:
                map.put(type, new KafkaZookeeper());
                break;
            case BROKER:
                map.put(type, new KafkaBroker(ipAddresses));
                break;
            case CONSUMER_PRIVATE:
                map.put(type, new KafkaConsumerPrivateGroup(ipAddresses, currentIP));
                break;
            case CONSUMER_SAME:
                map.put(type, new KafkaConsumerSameGroup(ipAddresses, currentIP, partition));
                break;
            case TOPIC_CREATOR:
                map.put(type, new KafkaCreatorTopic(ipAddresses, currentIP, ipAddresses.size()));
                break;
            default:
                throw new IllegalArgumentException("Invalid KafkaKingdomType");
        }
    }

    @Override
    public void shutDownGracefully() {
        if (map.isEmpty())
            return;

        if (!this.ipAddresses.isEmpty())
            this.ipAddresses.clear();

        map.get(KafkaKingdomType.CONSUMER_PRIVATE).Shutdown();
        map.get(KafkaKingdomType.CONSUMER_SAME).Shutdown();
        map.get(KafkaKingdomType.PRODUCER).Shutdown();
        map.get(KafkaKingdomType.TOPIC_CREATOR).Shutdown();
        map.get(KafkaKingdomType.BROKER).Shutdown();
        map.get(KafkaKingdomType.ZOOKEEPER).Shutdown();
        map.clear();
    }

    @Override
    public void manufactureKafkaComponent(KafkaKingdomType kafkaKingdomType) {
        map.get(kafkaKingdomType).constructKafkaComponentType();
    }

    @Override
    public <T extends IKafkaComponent> T getKafkaComponent(KafkaKingdomType type) {
        return (T) map.get(type);
    }

    @Override
    public String toString() {
        return "KafkaManufactureSmith{}";
    }
}
