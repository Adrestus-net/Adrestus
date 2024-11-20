package io.Adrestus.network;

import org.apache.kafka.clients.admin.NewTopic;

import java.util.Map;

public interface ITopic {
    void constructTopicName(Map<String,String> configs, int numPartitions);

    NewTopic getTopicName();
}
