package io.Adrestus.config;

public class KafkaConfiguration {

    public static final String ZOOKEEPER_HOST = "127.0.0.1";
    public static String KAFKA_HOST = "";
    public static final String ZOOKEEPER_PORT = "2181";
    public static final String KAFKA_PORT = "9092";
    public static final short KAFKA_REPLICATION_FACTOR = 1;
    public static final String TOPIC_GROUP_ID = "TOPIC_GROUP_ID";
    public static final String PRODUCER_GROUP_ID = "PRODUCER_GROUP_ID";
    public static final String CONSUMER_SAME_GROUP_ID = "CONSUMER_SAME_GROUP_ID";
    public static final String CONSUMER_PRIVATE_GROUP_ID = "CONSUMER_PRIVATE_GROUP_ID";


    public static final int EXECUTOR_TIMEOUT = 20;

    public static final int DISPERSE_PHASE1_RECEIVE_TIMEOUT = 8500;
    public static final int DISPERSE_PHASE2_RECEIVE_TIMEOUT = 8500;
    public static final int ANNOUNCE_PHASE_RECEIVE_TIMEOUT = 5500;
    public static final int PREPARE_PHASE_RECEIVE_TIMEOUT = 7500;
    public static final int COMMITTEE_PHASE_RECEIVE_TIMEOUT = 9550;

    public static final int PRIVATE_GROUP_METADATA_TIMEOUT = 65000;
}
