package io.Adrestus.network;

import io.Adrestus.rpc.RPCLogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

import static io.Adrestus.network.KafkaKingdomType.*;

public class KafkaManufactureSmith implements KafkaSmith {
    private final Map<KafkaKingdomType, IKafkaComponent> map;

    static {
        RPCLogger.getInstance();
    }

    public KafkaManufactureSmith(ArrayList<String> ipAddresses,String leader_host,int position, int partition) {
        this.map = new EnumMap<>(KafkaKingdomType.class);
        Arrays.stream(KafkaKingdomType.values()).forEach(type -> Init(type,ipAddresses,leader_host,position, partition, false));
    }


    private void Init(KafkaKingdomType type, ArrayList<String> ipAddresses,String leader_host,int position, int partition, boolean isClose) {
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
                map.put(type, new KafkaConsumerPrivateGroup(ipAddresses));
                break;
            case CONSUMER_SAME:
                map.put(type, new KafkaConsumerSameGroup(leader_host,position, partition));
                break;
            case TOPIC_CREATOR:
                map.put(type, new KafkaCreatorTopic(ipAddresses,leader_host, ipAddresses.size()-1));
                break;
            default:
                throw new IllegalArgumentException("Invalid KafkaKingdomType");
        }
    }

    @Override
    public void updateLeaderHost(KafkaKingdomType type,String leader_host,int position, int partition, boolean isClose) {
        if (isClose)
            map.get(KafkaKingdomType.CONSUMER_SAME).Shutdown();
        map.put(type, new KafkaConsumerSameGroup(leader_host,position, partition));
        map.get(KafkaKingdomType.CONSUMER_SAME).constructKafkaComponentType();
    }

    @Override
    public void shutDownGracefully() {
        if (map.isEmpty())
            return;
        map.entrySet()
                .stream()
                .filter(entry -> entry.getValue() != null)
                .forEach(entry -> entry.getValue().Shutdown());
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
