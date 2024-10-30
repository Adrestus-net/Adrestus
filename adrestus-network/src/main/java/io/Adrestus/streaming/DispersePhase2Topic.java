package io.Adrestus.streaming;

import io.Adrestus.config.KafkaConfiguration;
import org.apache.kafka.clients.admin.NewTopic;

public class DispersePhase2Topic implements ITopic {

    private NewTopic topicName;
    public DispersePhase2Topic() {
    }

    @Override
    public void constructTopicName(int numPartitions) {
        topicName = new NewTopic(KafkaConfiguration.DISPERSE_PHASE_2_TOPIC, numPartitions, KafkaConfiguration.KAFKA_REPLICATION_FACTOR);
    }

    @Override
    public NewTopic getTopicName() {
        return topicName;
    }

    @Override
    public String toString() {
        return "DispersePhase2Topic{}";
    }
}
