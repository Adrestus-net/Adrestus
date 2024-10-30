package io.Adrestus.streaming;

import io.Adrestus.config.KafkaConfiguration;
import org.apache.kafka.clients.admin.AdminClient;

import java.util.Properties;

public class KafkaCreatorTopic implements IKafkaComponent {

    private Properties props;
    private final int DispersePartitionSize;

    public KafkaCreatorTopic(int DispersePartitionSize) {
        this.DispersePartitionSize = DispersePartitionSize;
        TopicFactory.getInstance().constructTopicName(TopicType.PREPARE_PHASE, 1);
        TopicFactory.getInstance().constructTopicName(TopicType.COMMITTEE_PHASE, 1);
        TopicFactory.getInstance().constructTopicName(TopicType.DISPERSE_PHASE1, this.DispersePartitionSize);
        TopicFactory.getInstance().constructTopicName(TopicType.DISPERSE_PHASE2, 1);
        TopicFactory.getInstance().constructTopicName(TopicType.ANNOUNCE_PHASE, 1);
    }

    @Override
    public void constructKafkaComponentType() {
        props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("group.id", KafkaConfiguration.TOPIC_GROUP_ID + "-" + KafkaConfiguration.KAFKA_HOST);
        try (AdminClient adminClient = AdminClient.create(props)) {
            adminClient.createTopics(TopicFactory.getInstance().getCollectionTopics()).all().get();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public IKafkaComponent getKafkaKingdomType() {
        return this;
    }

    @Override
    public void Shutdown() {
        if(props == null) {
            return;
        }
        try (AdminClient adminClient = AdminClient.create(props)) {
            adminClient.deleteTopics(TopicFactory.getInstance().getCollectionTopicsNames()).all().get();
        } catch (Exception e) {
            e.printStackTrace();

        }
        props.clear();
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
