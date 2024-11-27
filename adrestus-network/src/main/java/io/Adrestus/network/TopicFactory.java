package io.Adrestus.network;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public class TopicFactory {
    private static Map<TopicType, ITopic> topicMap;
    private static volatile TopicFactory instance;
    private static volatile Collection<NewTopic> listTopic;
    private static volatile Collection<String> listTopicNames;

    private TopicFactory() {
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        }
        topicMap = new java.util.HashMap<>();
    }

    public static TopicFactory getInstance() {

        var result = instance;
        if (result == null) {
            synchronized (TopicFactory.class) {
                result = instance;
                if (result == null) {
                    result = new TopicFactory();
                    instance = result;
                }
            }
        }
        return result;
    }

    public void constructTopicName(TopicType type, Map<String, String> configs, int numPartitions) {
        if (topicMap.containsKey(type))
            return;
        ITopic topic = type.getConstructor().get();
        topic.constructTopicName(configs, numPartitions);
        topicMap.put(type, topic);
    }

    public NewTopic getTopicName(TopicType type) {
        if (!topicMap.containsKey(type))
            throw new IllegalArgumentException("Topic not found");
        return topicMap.get(type).getTopicName();
    }

    public Collection<NewTopic> getCollectionTopics() {
        if (listTopic == null)
            listTopic = topicMap.values().stream().map(ITopic::getTopicName).collect(Collectors.toSet());
        return listTopic;
    }

    public Collection<String> getSingleTopicCollection(TopicType type) {
        if (listTopicNames == null)
            listTopicNames = topicMap
                    .values()
                    .stream()
                    .map(topic -> topic.getTopicName().name())
                    .filter(topic_name -> topic_name.equals(type.name())).collect(Collectors.toSet());
        return listTopicNames;
    }

    public Collection<String> getCollectionTopicsNames() {
        if (listTopicNames == null)
            listTopicNames = topicMap
                    .values()
                    .stream()
                    .map(topic -> topic.getTopicName().name())
                    .filter(topic_name -> !topic_name.equals(TopicType.DISPERSE_PHASE1.name())).collect(Collectors.toSet());
        return listTopicNames;
    }

    public Collection<String> getAllCollectionTopicsNamesAsString() {
        if (listTopicNames == null)
            listTopicNames = topicMap
                    .values()
                    .stream()
                    .map(topic -> topic.getTopicName().name())
                    .collect(Collectors.toSet());
        return listTopicNames;
    }

    @Override
    public String toString() {
        return "TopicFactory{}";
    }
}
