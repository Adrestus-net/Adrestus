package io.Adrestus.network;

import org.apache.kafka.clients.admin.NewTopic;

public interface ITopic {
    void constructTopicName(int numPartitions);

    NewTopic getTopicName();
}
