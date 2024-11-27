package io.Adrestus.network;

import io.Adrestus.config.KafkaConfiguration;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;

import java.util.Map;

public class DispersePhase2Topic implements ITopic {

    private NewTopic topicName;
    private TopicPartition topicPartition;

    public DispersePhase2Topic() {
    }

    @Override
    public void constructTopicName(Map<String, String> configs, int numPartitions) {
        topicName = new NewTopic(TopicType.DISPERSE_PHASE2.name(), numPartitions, KafkaConfiguration.KAFKA_REPLICATION_FACTOR);
        topicPartition = new TopicPartition(TopicType.DISPERSE_PHASE2.name(), numPartitions);
        topicName.configs(configs);
    }


    @Override
    public TopicPartition getTopicPartition() {
        return topicPartition;
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
