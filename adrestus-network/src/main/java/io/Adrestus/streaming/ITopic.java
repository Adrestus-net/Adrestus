package io.Adrestus.streaming;

import org.apache.kafka.clients.admin.NewTopic;

public interface ITopic {
    void constructTopicName(int numPartitions);

    NewTopic getTopicName();
}