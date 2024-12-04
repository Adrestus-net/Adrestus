package io.Adrestus.network;

public interface KafkaSmith {

    void manufactureKafkaComponent(KafkaKingdomType kafkaKingdomType);

    <T extends IKafkaComponent> T getKafkaComponent(KafkaKingdomType type);

    void shutDownGracefully();
}
