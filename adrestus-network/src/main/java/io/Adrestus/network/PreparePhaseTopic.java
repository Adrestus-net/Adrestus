package io.Adrestus.network;

import io.Adrestus.config.KafkaConfiguration;
import org.apache.kafka.clients.admin.NewTopic;

public class PreparePhaseTopic implements ITopic {

    private NewTopic topicName;

    public PreparePhaseTopic() {
    }

    @Override
    public void constructTopicName(int numPartitions) {
        topicName = new NewTopic(TopicType.PREPARE_PHASE.name(), numPartitions, KafkaConfiguration.KAFKA_REPLICATION_FACTOR);
    }

    @Override
    public NewTopic getTopicName() {
        return topicName;
    }


    @Override
    public String toString() {
        return "PreparePhaseTopic{}";
    }
}
