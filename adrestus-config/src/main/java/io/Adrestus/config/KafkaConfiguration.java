package io.Adrestus.config;

public class KafkaConfiguration {

    public static final String ZOOKEEPER_HOST = "localhost";
    public static String KAFKA_HOST = "";
    public static final String ZOOKEEPER_PORT = "2181";
    public static final String KAFKA_PORT = "9092";
    public static final short KAFKA_REPLICATION_FACTOR = 1;
    public static final String TOPIC_GROUP_ID = "TOPIC_GROUP_ID";
    public static final String PRODUCER_GROUP_ID = "PRODUCER_GROUP_ID";
    public static final String CONSUMER_SAME_GROUP_ID = "CONSUMER_SAME_GROUP_ID";
    public static final String CONSUMER_PRIVATE_GROUP_ID = "CONSUMER_PRIVATE_GROUP_ID";


    public static final int RECEIVE_TIMEOUT = 4000;
    public static final int EXECUTOR_TIMEOUT = 12;

    public static final int PRIVATE_GROUP_METADATA_TIMEOUT = 65000;
}
