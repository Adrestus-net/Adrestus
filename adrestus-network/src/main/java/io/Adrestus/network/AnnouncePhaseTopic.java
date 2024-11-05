package io.Adrestus.network;

import io.Adrestus.config.KafkaConfiguration;
import org.apache.kafka.clients.admin.NewTopic;

public class AnnouncePhaseTopic implements ITopic {

    private NewTopic topicName;

    public AnnouncePhaseTopic() {
    }

    @Override
    public String toString() {
        return "AnnouncePhaseTopic{}";
    }

    @Override
    public void constructTopicName(int numPartitions) {
        topicName = new NewTopic(TopicType.ANNOUNCE_PHASE.name(), numPartitions, KafkaConfiguration.KAFKA_REPLICATION_FACTOR);
    }

    @Override
    public NewTopic getTopicName() {
        return topicName;
    }
}
