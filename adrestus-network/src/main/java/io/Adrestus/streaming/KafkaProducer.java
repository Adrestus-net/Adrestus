package io.Adrestus.streaming;

import io.Adrestus.config.KafkaConfiguration;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Objects;
import java.util.Properties;

public class KafkaProducer implements IKafkaComponent {
    private final Properties props;
    private Producer<String, String> producer;

    public KafkaProducer() {
        this.props = new Properties();
    }

    @Override
    public void constructKafkaComponentType() {
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaConfiguration.KAFKA_HOST + ":" + KafkaConfiguration.KAFKA_PORT);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, KafkaConfiguration.PRODUCER_GROUP_ID + "-" + KafkaConfiguration.KAFKA_HOST);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, "1");
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4");
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, "200000");
        props.put(ProducerConfig.LINGER_MS_CONFIG, "100");
//        props.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, RoundRobinPartitioner.class.getName());
//        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
        producer = new org.apache.kafka.clients.producer.KafkaProducer<>(props);
    }

    public void produceMessage(String topic, String key, String value) {
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, value);
        producer.send(record);
        producer.flush();
    }

    public void produceMessage(String topic, int partition, String key, String value) {
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, partition, key, value);
        producer.send(record);
        producer.flush();
    }

    @Override
    public IKafkaComponent getKafkaKingdomType() {
        return this;
    }

    @Override
    public void Shutdown() {
        if(this.producer == null)
            return;
        this.producer.close();
        this.props.clear();
    }

    public Properties getProps() {
        return props;
    }

    public Producer<String, String> getProducer() {
        return producer;
    }

    public void setProducer(Producer<String, String> producer) {
        this.producer = producer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KafkaProducer that = (KafkaProducer) o;
        return Objects.equals(props, that.props) && Objects.equals(producer, that.producer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(props, producer);
    }

    @Override
    public String toString() {
        return "KafkaProducer{" +
                "props=" + props +
                ", producer=" + producer +
                '}';
    }
}
