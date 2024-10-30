package io.Adrestus.streaming;

import io.Adrestus.config.KafkaConfiguration;
import org.apache.kafka.clients.admin.NewTopic;

public class PreparePhaseTopic implements ITopic {

    private NewTopic topicName;

    public PreparePhaseTopic() {
    }

    @Override
    public void constructTopicName(int numPartitions) {
        topicName = new NewTopic(KafkaConfiguration.PREPARE_PHASE_TOPIC, numPartitions, KafkaConfiguration.KAFKA_REPLICATION_FACTOR);
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
