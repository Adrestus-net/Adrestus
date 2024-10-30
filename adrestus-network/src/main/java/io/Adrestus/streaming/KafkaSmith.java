package io.Adrestus.streaming;

import java.util.ArrayList;

public interface KafkaSmith {

    void manufactureKafkaComponent(KafkaKingdomType kafkaKingdomType);

    <T extends IKafkaComponent> T getKafkaComponent(KafkaKingdomType type);

    void  updateLeaderHost(KafkaKingdomType type,ArrayList<String> ipAddresses, String leader_host,int partition, boolean isClose);

    void shutDownGracefully();
}
