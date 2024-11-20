package io.Adrestus.network;

import io.Adrestus.config.KafkaConfiguration;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.RoundRobinAssignor;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Objects;
import java.util.Properties;

public class KafkaConsumerSameGroup implements IKafkaComponent, Cloneable {
    private final Properties props;
    private final String leader_host;
    private final int position;
    private final int partition;
    private Consumer<String, byte[]> consumer;

    public KafkaConsumerSameGroup() {
        this.props = new Properties();
        this.leader_host = "";
        this.partition = 0;
        this.position = 0;
    }

    public KafkaConsumerSameGroup(String leader_host, int position, int partition) {
        this.props = new Properties();
        this.leader_host = leader_host;
        this.position = position;
        this.partition = partition;
    }

    @Override
    public void constructKafkaComponentType() {
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, this.leader_host + ":" + KafkaConfiguration.KAFKA_PORT);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, KafkaConfiguration.CONSUMER_SAME_GROUP_ID);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 1);
        props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 10);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 60000);
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "600000");
        props.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, "1000");
        props.put(ConsumerConfig.METADATA_MAX_AGE_CONFIG, "1000");
        props.put(ConsumerConfig.RECONNECT_BACKOFF_MS_CONFIG, "500");
        props.put(ConsumerConfig.RECONNECT_BACKOFF_MAX_MS_CONFIG, "8000");
        props.put(ConsumerConfig.RETRY_BACKOFF_MS_CONFIG, "500");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG, RoundRobinAssignor.class.getName());
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT");
        props.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
        props.put(SaslConfigs.SASL_JAAS_CONFIG, "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"" + "consumer" + "-" + this.position + "-" + this.leader_host + "\" password=\"consumer-secret\";");

//        System.out.println("1 "+props.getProperty(SaslConfigs.SASL_JAAS_CONFIG));
//        System.out.println("1 "+props.getProperty(ConsumerConfig.GROUP_ID_CONFIG));
//        System.out.println("1 "+props.getProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG));
//        System.out.println();

        consumer = new KafkaConsumer<>(props);
        TopicPartition partition = new TopicPartition(TopicFactory.getInstance().getTopicName(TopicType.DISPERSE_PHASE1).name(), this.partition);
        consumer.partitionsFor(TopicFactory.getInstance().getTopicName(TopicType.DISPERSE_PHASE1).name(), Duration.ofMillis(KafkaConfiguration.PRIVATE_GROUP_METADATA_TIMEOUT));
        consumer.assign(Collections.singleton(partition));
//        while (consumer.assignment().isEmpty()) {
//            consumer.poll(Duration.ofMillis(100));
//        }
//        Set<TopicPartition> assignedPartitions = consumer.assignment();
//        for (TopicPartition partition1 : assignedPartitions) {
//            System.out.println("Node "+this.partition+" "+"Assigned to partition: " + partition1.partition());
//        }
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
