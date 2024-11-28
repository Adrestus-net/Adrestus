package io.Adrestus.network;

import io.Adrestus.config.KafkaConfiguration;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.RoundRobinAssignor;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.Objects;
import java.util.Properties;

public class KafkaConsumerSameGroup implements IKafkaComponent, Cloneable {
    private static final Logger LOG = LoggerFactory.getLogger(KafkaConsumerSameGroup.class);

    private final Properties props;
    private final String leader_host;
    private final String currentIP;
    private final int partition;
    private Consumer<String, byte[]> consumer;

    public KafkaConsumerSameGroup() {
        this.props = new Properties();
        this.leader_host = "";
        this.currentIP = "";
        this.partition = 0;
    }

    public KafkaConsumerSameGroup(String leader_host, String currentIP, int partition) {
        this.props = new Properties();
        this.leader_host = leader_host;
        this.partition = partition;
        this.currentIP = currentIP;
    }

    @Override
    public void constructKafkaComponentType() {
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, this.leader_host + ":" + KafkaConfiguration.KAFKA_PORT);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, KafkaConfiguration.CONSUMER_SAME_GROUP_ID);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, "7000");
        props.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, "13107200");
        props.put(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, "536870912");
        props.put(ConsumerConfig.RECEIVE_BUFFER_CONFIG, "-1");
        props.put(ConsumerConfig.SEND_BUFFER_CONFIG, "-1");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, "1");
        props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, "10");
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "60000");
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "600000");
        props.put(ConsumerConfig.AUTO_INCLUDE_JMX_REPORTER_CONFIG, "false");
        props.put(ConsumerConfig.ENABLE_METRICS_PUSH_CONFIG, "FALSE");
        props.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, "1000");
        //props.put(ConsumerConfig.METADATA_MAX_AGE_CONFIG, "1000");
        props.put(ConsumerConfig.RETRY_BACKOFF_MS_CONFIG, "300");
        props.put(ConsumerConfig.RETRY_BACKOFF_MAX_MS_CONFIG, "7000");
        props.put(ConsumerConfig.RECONNECT_BACKOFF_MS_CONFIG, "300");
        props.put(ConsumerConfig.RECONNECT_BACKOFF_MAX_MS_CONFIG, "7000");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG, RoundRobinAssignor.class.getName());
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT");
        props.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
        props.put(SaslConfigs.SASL_JAAS_CONFIG, "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"" + "consumer" + "-" + this.partition + "-" + this.currentIP + "\" password=\"consumer-secret\";");

        consumer = new KafkaConsumer<>(props);
        TopicPartition partition = new TopicPartition(TopicType.DISPERSE_PHASE1.name(), this.partition);
        consumer.partitionsFor(TopicType.DISPERSE_PHASE1.name());
        consumer.assign(Collections.singleton(partition));
        LOG.info("Node " + this.partition + " " + "Subscribing to partition: " + partition.partition());
    }


    @Override
    public IKafkaComponent getKafkaKingdomType() {
        return this;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public void Shutdown() {
        if (this.consumer == null)
            return;
        this.consumer.wakeup();
        this.consumer.close();
        this.props.clear();
    }

    @Override
    public String toString() {
        return "KafkaConsumerSameGroup{}";
    }

    public Properties getProps() {
        return props;
    }

    public Consumer<String, byte[]> getConsumer() {
        return consumer;
    }

    public void setConsumer(Consumer<String, byte[]> consumer) {
        this.consumer = consumer;
    }


    public String getHost() {
        return leader_host;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KafkaConsumerSameGroup that = (KafkaConsumerSameGroup) o;
        return Objects.equals(props, that.props) && Objects.equals(leader_host, that.leader_host) && Objects.equals(consumer, that.consumer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(props, leader_host, consumer);
    }

}
