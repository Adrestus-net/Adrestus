package io.Adrestus.network;

import io.Adrestus.config.KafkaConfiguration;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;

import java.util.Map;

public class CommitPhaseTopic implements ITopic {

    private NewTopic topicName;
    private TopicPartition topicPartition;

    public CommitPhaseTopic() {
    }

    @Override
    public void constructTopicName(Map<String, String> configs, int numPartitions) {
        topicName = new NewTopic(TopicType.COMMITTEE_PHASE.name(), numPartitions, KafkaConfiguration.KAFKA_REPLICATION_FACTOR);
        topicPartition = new TopicPartition(TopicType.COMMITTEE_PHASE.name(), numPartitions - 1);
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
        return "CommitPhaseTopic{}";
    }
}
