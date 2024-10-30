package io.Adrestus.streaming;

import io.Adrestus.config.KafkaConfiguration;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

public class KafkaConsumerSameGroup implements IKafkaComponent, Cloneable {
    private final Properties props;
    private final String host;
    private final int partition;
    private Consumer<String, String> consumer;

    public KafkaConsumerSameGroup() {
        this.props = new Properties();
        this.host = "";
        this.partition = 0;
    }

    public KafkaConsumerSameGroup(String host, int partition) {
        this.props = new Properties();
        this.host = host;
        this.partition = partition;
    }

    @Override
    public void constructKafkaComponentType() {
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, this.host + ":" + KafkaConfiguration.KAFKA_PORT);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, KafkaConfiguration.CONSUMER_SAME_GROUP_ID);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, "100000");
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "500");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, "100");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG, RoundRobinAssignor.class.getName());
        consumer = new KafkaConsumer<>(props);
        TopicPartition partition = new TopicPartition(TopicFactory.getInstance().getTopicName(TopicType.DISPERSE_PHASE1).name(), this.partition);
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

    public Consumer<String, String> getConsumer() {
        return consumer;
    }

    public void setConsumer(Consumer<String, String> consumer) {
        this.consumer = consumer;
    }


    public String getHost() {
        return host;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KafkaConsumerSameGroup that = (KafkaConsumerSameGroup) o;
        return Objects.equals(props, that.props) && Objects.equals(host, that.host) && Objects.equals(consumer, that.consumer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(props, host, consumer);
    }

}
