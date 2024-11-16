package io.Adrestus.network;

import java.util.ArrayList;

public interface KafkaSmith {

    void manufactureKafkaComponent(KafkaKingdomType kafkaKingdomType);

    <T extends IKafkaComponent> T getKafkaComponent(KafkaKingdomType type);

    void updateACLKafkaList(ArrayList<String> ipAddresses);

    void updateLeaderHost(KafkaKingdomType type, ArrayList<String> ipAddresses, String leader_host, int partition, boolean isClose);

    void shutDownGracefully();
}
