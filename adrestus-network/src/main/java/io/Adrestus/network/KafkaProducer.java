package io.Adrestus.network;

import io.Adrestus.config.KafkaConfiguration;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RoundRobinPartitioner;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Objects;
import java.util.Properties;

public class KafkaProducer implements IKafkaComponent {
    private final Properties props;
    private Producer<String, byte[]> producer;

    public KafkaProducer() {
        this.props = new Properties();
    }

    @Override
    public void constructKafkaComponentType() {
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaConfiguration.KAFKA_HOST + ":" + KafkaConfiguration.KAFKA_PORT);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, KafkaConfiguration.PRODUCER_GROUP_ID + "-" + KafkaConfiguration.KAFKA_HOST);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, "1");
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4");
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, "200000");
        props.put(ProducerConfig.LINGER_MS_CONFIG, "100");
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT");
        props.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
        props.put(SaslConfigs.SASL_JAAS_CONFIG, "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"producer\" password=\"producer-secret\";");
        props.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, RoundRobinPartitioner.class.getName());
//        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
        producer = new org.apache.kafka.clients.producer.KafkaProducer<>(props);
    }

    public void produceMessage(String topic, String key, byte[] data) {
        ProducerRecord<String, byte[]> record = new ProducerRecord<>(topic, key, data);
        producer.send(record);
    }

    public void produceMessage(String topic, int partition, String key, byte[] data) {
        ProducerRecord<String, byte[]> record = new ProducerRecord<>(topic, partition, key, data);
        producer.send(record);
    }

    @Override
    public IKafkaComponent getKafkaKingdomType() {
        return this;
    }

    @Override
    public void Shutdown() {
        if (this.producer == null)
            return;
        this.producer.close();
        this.props.clear();
    }

    public Properties getProps() {
        return props;
    }

    public Producer<String, byte[]> getProducer() {
        return producer;
    }

    public void setProducer(Producer<String, byte[]> producer) {
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
