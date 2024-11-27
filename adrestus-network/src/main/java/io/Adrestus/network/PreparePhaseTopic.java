package io.Adrestus.network;

import io.Adrestus.config.KafkaConfiguration;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;

import java.util.Map;

public class PreparePhaseTopic implements ITopic {

    private NewTopic topicName;
    private TopicPartition topicPartition;

    public PreparePhaseTopic() {
    }

    @Override
    public void constructTopicName(Map<String, String> configs, int numPartitions) {
        topicName = new NewTopic(TopicType.PREPARE_PHASE.name(), numPartitions, KafkaConfiguration.KAFKA_REPLICATION_FACTOR);
        topicPartition = new TopicPartition(TopicType.PREPARE_PHASE.name(), numPartitions);
        topicName.configs(configs);
    }

    @Override
    public NewTopic getTopicName() {
        return topicName;
    }

    @Override
    public TopicPartition getTopicPartition() {
        return topicPartition;
    }


    @Override
    public String toString() {
        return "PreparePhaseTopic{}";
    }
}
