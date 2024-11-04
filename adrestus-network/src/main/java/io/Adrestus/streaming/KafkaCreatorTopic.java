package io.Adrestus.streaming;

import io.Adrestus.config.KafkaConfiguration;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.TopicListing;

import java.util.Collections;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

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
        props.put("bootstrap.servers", KafkaConfiguration.KAFKA_HOST + ":" + "9092");
        props.put("group.id", KafkaConfiguration.TOPIC_GROUP_ID + "-" + KafkaConfiguration.KAFKA_HOST);

        boolean isTopicExist = false;
        try (AdminClient adminClient = AdminClient.create(props)) {
            Set<String> existingTopics = adminClient
                    .listTopics().listings().get().stream()
                    .map(TopicListing::name)
                    .collect(Collectors.toSet());

            for (NewTopic topic : TopicFactory.getInstance().getCollectionTopics()) {
                if (!existingTopics.contains(topic.name())) {
                    adminClient.createTopics(Set.of(topic)).all().get();
                }
            }
        } catch (ExecutionException e) {
            if (e.getCause() instanceof org.apache.kafka.common.errors.TopicExistsException) {
                System.out.println("Topics already exist");
                isTopicExist = true;
            } else {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (isTopicExist) {
            try (AdminClient adminClient = AdminClient.create(props)) {
                adminClient.deleteTopics(Collections.singleton(TopicFactory.getInstance().getTopicName(TopicType.DISPERSE_PHASE1).name())).all().get();
                boolean isDeleted = false;
                while (!isDeleted) {
                    Set<String> existingTopics = adminClient.listTopics().names().get();
                    isDeleted = !existingTopics.contains(TopicFactory.getInstance().getTopicName(TopicType.DISPERSE_PHASE1).name());
                    if (!isDeleted) {
                        Thread.sleep(1000); // Wait for 100 milliseconds before checking again
                    }
                }
            } catch (ExecutionException | InterruptedException ex) {
                ex.printStackTrace();
            }
            try (AdminClient adminClient = AdminClient.create(props)) {
                NewTopic topic = TopicFactory.getInstance().getTopicName(TopicType.DISPERSE_PHASE1);
                adminClient.createTopics(Set.of(topic)).all().get();
            } catch (ExecutionException | InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public IKafkaComponent getKafkaKingdomType() {
        return this;
    }

    @Override
    public void Shutdown() {
        if (props == null) {
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
