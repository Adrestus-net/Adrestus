package io.Adrestus.streaming;

import io.Adrestus.config.KafkaConfiguration;
import org.apache.kafka.clients.admin.NewTopic;

public class CommitPhaseTopic implements ITopic {

    private NewTopic topicName;
    public CommitPhaseTopic() {
    }

    @Override
    public void constructTopicName(int numPartitions) {
        topicName = new NewTopic(TopicType.COMMITTEE_PHASE.name(), numPartitions, KafkaConfiguration.KAFKA_REPLICATION_FACTOR);
    }

    @Override
    public NewTopic getTopicName() {
        return topicName;
    }

    @Override
    public String toString() {
        return "CommitPhaseTopic{}";
    }
}
