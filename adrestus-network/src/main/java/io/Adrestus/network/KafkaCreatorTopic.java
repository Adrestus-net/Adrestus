package io.Adrestus.network;

import io.Adrestus.config.KafkaConfiguration;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.acl.*;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.errors.TopicAuthorizationException;
import org.apache.kafka.common.errors.TopicDeletionDisabledException;
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourcePattern;
import org.apache.kafka.common.resource.ResourcePatternFilter;
import org.apache.kafka.common.resource.ResourceType;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class KafkaCreatorTopic implements IKafkaComponent {

    private Properties props;
    private final int DispersePartitionSize;
    private final String leader_host;
    private final ArrayList<String> ipAddresses;
    private final ArrayList<AclBindingFilter> bindingFilters;
    private final List<AclBinding> aclBindings;
    private AdminClient adminClient;

    public KafkaCreatorTopic(ArrayList<String> ipAddresses, String leader_host, int DispersePartitionSize) {
        this.ipAddresses = ipAddresses;
        this.DispersePartitionSize = DispersePartitionSize;
        this.leader_host = leader_host;
        this.bindingFilters = new ArrayList<>();
        this.aclBindings = new ArrayList<>();
        TopicFactory.getInstance().constructTopicName(TopicType.PREPARE_PHASE, 1);
        TopicFactory.getInstance().constructTopicName(TopicType.COMMITTEE_PHASE, 1);
        TopicFactory.getInstance().constructTopicName(TopicType.DISPERSE_PHASE1, this.DispersePartitionSize);
        TopicFactory.getInstance().constructTopicName(TopicType.DISPERSE_PHASE2, 1);
        TopicFactory.getInstance().constructTopicName(TopicType.ANNOUNCE_PHASE, 1);
    }

    @Override
    public void constructKafkaComponentType() {
        props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaConfiguration.KAFKA_HOST + ":" + "9092");
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT");
        props.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
        props.put(SaslConfigs.SASL_JAAS_CONFIG, "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"admin\" password=\"admin-secret\";");
        props.put("group.id", KafkaConfiguration.TOPIC_GROUP_ID + "-" + KafkaConfiguration.KAFKA_HOST);


        adminClient = AdminClient.create(props);
        try {
            // Grant `CREATE` permission on the Cluster level to allow topic creation
            AclBinding clusterCreateAcl = new AclBinding(
                    new ResourcePattern(ResourceType.CLUSTER, "kafka-cluster", PatternType.LITERAL),
                    new org.apache.kafka.common.acl.AccessControlEntry("User:admin", "*", AclOperation.CREATE, AclPermissionType.ALLOW)
            );

            // Grant `CREATE` permission on all topics
            AclBinding topicCreateAcl = new AclBinding(
                    new ResourcePattern(ResourceType.TOPIC, "*", PatternType.LITERAL),
                    new org.apache.kafka.common.acl.AccessControlEntry("User:admin", "*", AclOperation.CREATE, AclPermissionType.ALLOW)
            );

            // Optional: Grant `DESCRIBE` permission on the Cluster level to access metadata
            AclBinding clusterDescribeAcl = new AclBinding(
                    new ResourcePattern(ResourceType.CLUSTER, "kafka-cluster", PatternType.LITERAL),
                    new org.apache.kafka.common.acl.AccessControlEntry("User:admin", "*", AclOperation.DESCRIBE, AclPermissionType.ALLOW)
            );

            // Apply ACLs
            adminClient.createAcls(Arrays.asList(clusterCreateAcl, topicCreateAcl, clusterDescribeAcl)).all().get();

            Set<String> existingTopics = adminClient
                    .listTopics().listings().get().stream()
                    .map(TopicListing::name)
                    .collect(Collectors.toSet());

            for (NewTopic topic : TopicFactory.getInstance().getCollectionTopics()) {
                if (!existingTopics.contains(topic.name())) {
                    adminClient.createTopics(Set.of(topic)).all().get();
                }
            }

            for (NewTopic topic : TopicFactory.getInstance().getCollectionTopics()) {

                ResourcePattern resourcePattern = new ResourcePattern(ResourceType.TOPIC, topic.name(), PatternType.LITERAL);
                // Create ACL entries for a producer
                AccessControlEntry producerWriteEntry = new AccessControlEntry("User:producer", "localhost", AclOperation.WRITE, AclPermissionType.ALLOW);
                AccessControlEntry producerDescribeEntry = new AccessControlEntry("User:producer", "localhost", AclOperation.DESCRIBE, AclPermissionType.ALLOW);
                AclBinding producerWriteAcl = new AclBinding(resourcePattern, producerWriteEntry);
                AclBinding producerDescribeAcl = new AclBinding(resourcePattern, producerDescribeEntry);
                aclBindings.add(producerWriteAcl);
                aclBindings.add(producerDescribeAcl);

                AclBindingFilter producerReadfilter = new AclBindingFilter(resourcePattern.toFilter(), producerWriteEntry.toFilter());
                AclBindingFilter producerDescribefilter = new AclBindingFilter(resourcePattern.toFilter(), producerDescribeEntry.toFilter());
                bindingFilters.add(producerReadfilter);
                bindingFilters.add(producerDescribefilter);

                // Create ACL entries for a consumer
                for (int index = 0; index < ipAddresses.size(); index++) {
                    String ip = ipAddresses.get(index);
                    if (ip.equals(leader_host) && !leader_host.equals("localhost"))
                        continue;
                    AccessControlEntry consumerReadEntry = new AccessControlEntry("User:consumer" + "-" + index + "-" + ip, ip, AclOperation.READ, AclPermissionType.ALLOW);
                    AccessControlEntry consumerDescribeEntry = new AccessControlEntry("User:consumer" + "-" + index + "-" + ip, ip, AclOperation.DESCRIBE, AclPermissionType.ALLOW);
                    AclBinding consumerReadAcl = new AclBinding(resourcePattern, consumerReadEntry);
                    AclBinding consumerDescribeAcl = new AclBinding(resourcePattern, consumerDescribeEntry);
                    aclBindings.add(consumerReadAcl);
                    aclBindings.add(consumerDescribeAcl);
                    // Verify the ACLs
                    AclBindingFilter Readfilter = new AclBindingFilter(new ResourcePatternFilter(ResourceType.TOPIC, topic.name(), PatternType.LITERAL), consumerReadEntry.toFilter());
                    AclBindingFilter Describefilter = new AclBindingFilter(new ResourcePatternFilter(ResourceType.TOPIC, topic.name(), PatternType.LITERAL), consumerDescribeEntry.toFilter());
                    bindingFilters.add(Readfilter);
                    bindingFilters.add(Describefilter);
                }
            }
            adminClient.createAcls(aclBindings).all().get();
//            bindingFilters.stream().forEach(filter -> {
//                //Print the ACLs
//                try {
//                    adminClient.describeAcls(filter).values().get().forEach(System.out::println);
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                } catch (ExecutionException e) {
//                    throw new RuntimeException(e);
//                }
//            });
//            int g = 3;
        } catch (ExecutionException e) {
            if (e.getCause() instanceof org.apache.kafka.common.errors.TopicExistsException) {
                System.out.println("Topics already exist");
            } else {
                e.printStackTrace();
            }
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

        // Delete the ACLs
        adminClient.deleteAcls(bindingFilters).all().whenComplete((aVoid, throwable) -> {
            if (throwable != null) {
                throwable.printStackTrace();
            }
        });

        final DeleteTopicsResult deleteTopicsResult = adminClient.deleteTopics(TopicFactory.getInstance().getCollectionTopicsNames());
        final Map<String, KafkaFuture<Void>> results = deleteTopicsResult.topicNameValues();
        for (final Map.Entry<String, KafkaFuture<Void>> entry : results.entrySet()) {
            try {
                entry.getValue().get(10, TimeUnit.SECONDS);
            } catch (final Exception e) {
                final Throwable rootCause = ExceptionUtils.getRootCause(e);

                if (rootCause instanceof TopicDeletionDisabledException) {
                    throw new TopicDeletionDisabledException("Topic deletion is disabled. "
                            + "To delete the topic, you must set '" + "' to true in "
                            + "the Kafka broker configuration.");
                } else if (rootCause instanceof TopicAuthorizationException) {
                    e.printStackTrace();
                } else if (!(rootCause instanceof UnknownTopicOrPartitionException)) {
                    e.printStackTrace();
                }
            } finally {
                adminClient.close(Duration.ofSeconds(5));
            }

            this.bindingFilters.clear();
            this.aclBindings.clear();
            this.props.clear();
            this.ipAddresses.clear();
        }
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


