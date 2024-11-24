package io.Adrestus.network;

import io.Adrestus.config.KafkaConfiguration;
import org.apache.kafka.clients.admin.NewTopic;

import java.util.Map;

public class DispersePhase1Topic implements ITopic {

    private NewTopic topicName;

    public DispersePhase1Topic() {
    }

    @Override
    public void constructTopicName(Map<String, String> configs, int numPartitions) {
        topicName = new NewTopic(TopicType.DISPERSE_PHASE1.name(), numPartitions, KafkaConfiguration.KAFKA_REPLICATION_FACTOR);
        topicName.configs(configs);
    }

    @Override
    public NewTopic getTopicName() {
        return topicName;
    }

    @Override
    public String toString() {
        return "DispersePhase1Topic{}";
    }
}
