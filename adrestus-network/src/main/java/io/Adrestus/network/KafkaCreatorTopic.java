package io.Adrestus.network;

import io.Adrestus.config.ConsensusConfiguration;
import io.Adrestus.config.KafkaConfiguration;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.TopicListing;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.acl.*;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourcePattern;
import org.apache.kafka.common.resource.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class KafkaCreatorTopic implements IKafkaComponent {
    private static Logger LOG = LoggerFactory.getLogger(KafkaCreatorTopic.class);

    private Properties props;
    private final int DispersePartitionSize;
    private final String currentIP;
    private final ArrayList<String> ipAddresses;
    private final ArrayList<AclBindingFilter> bindingFilters;
    private final List<AclBinding> aclBindings;
    private final Map<String, String> configs;
    private AdminClient adminClient;

    public KafkaCreatorTopic(ArrayList<String> ipAddresses, String currentIP, int DispersePartitionSize) {
        this.ipAddresses = ipAddresses;
        this.DispersePartitionSize = DispersePartitionSize;
        this.currentIP = currentIP;
        this.bindingFilters = new ArrayList<>();
        this.aclBindings = new ArrayList<>();
        this.configs = new HashMap<>();
        this.setConfigs();
        TopicFactory.getInstance().constructTopicName(TopicType.PREPARE_PHASE, this.configs, 1);
        TopicFactory.getInstance().constructTopicName(TopicType.COMMITTEE_PHASE, this.configs, 1);
        TopicFactory.getInstance().constructTopicName(TopicType.DISPERSE_PHASE1, this.configs, this.DispersePartitionSize);
        TopicFactory.getInstance().constructTopicName(TopicType.DISPERSE_PHASE2, this.configs, 1);
        TopicFactory.getInstance().constructTopicName(TopicType.ANNOUNCE_PHASE, this.configs, 1);
    }

    private void setConfigs() {
        this.configs.put("cleanup.policy", "compact");
        this.configs.put("compression.type", "lz4");
        this.configs.put("index.interval.bytes", "40960");
        this.configs.put("max.message.bytes", "2024000000");
        this.configs.put("min.insync.replicas", "1");
        this.configs.put("retention.ms",  String.valueOf(ConsensusConfiguration.EPOCH_TRANSITION * 2 * ConsensusConfiguration.CONSENSUS_TIMER));
        this.configs.put("segment.ms", String.valueOf(ConsensusConfiguration.EPOCH_TRANSITION * 2 * ConsensusConfiguration.CONSENSUS_TIMER));
        this.configs.put("segment.bytes", "1342177280");
    }
    @Override
    public void constructKafkaComponentType() {
        props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaConfiguration.KAFKA_HOST + ":" + "9092");
        props.put(AdminClientConfig.RECEIVE_BUFFER_CONFIG, "-1");
        props.put(AdminClientConfig.SEND_BUFFER_CONFIG, "-1");
        props.put(AdminClientConfig.AUTO_INCLUDE_JMX_REPORTER_DOC, "false");
        props.put(AdminClientConfig.ENABLE_METRICS_PUSH_CONFIG, "false");
        props.put(AdminClientConfig.METADATA_RECOVERY_STRATEGY_CONFIG, "REBOOTSTRAP");
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT");
        props.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
        props.put(SaslConfigs.SASL_JAAS_CONFIG, "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"admin\" password=\"admin-secret\";");
        props.put("group.id", KafkaConfiguration.TOPIC_GROUP_ID + "-" + KafkaConfiguration.KAFKA_HOST);


        adminClient = AdminClient.create(props);
        try {
            // Grant `CREATE` permission on the Cluster level to allow topic creation
            AclBinding clusterCreateAcl = new AclBinding(
                    new ResourcePattern(ResourceType.CLUSTER, "kafka-cluster", PatternType.LITERAL),
                    new org.apache.kafka.common.acl.AccessControlEntry("User:admin", "127.0.0.1", AclOperation.CREATE, AclPermissionType.ALLOW)
            );

            // Grant `CREATE` permission on all topics
            AclBinding topicCreateAcl = new AclBinding(
                    new ResourcePattern(ResourceType.TOPIC, "*", PatternType.LITERAL),
                    new org.apache.kafka.common.acl.AccessControlEntry("User:admin", "127.0.0.1", AclOperation.CREATE, AclPermissionType.ALLOW)
            );

            // Optional: Grant `DESCRIBE` permission on the Cluster level to access metadata
            AclBinding clusterDescribeAcl = new AclBinding(
                    new ResourcePattern(ResourceType.CLUSTER, "kafka-cluster", PatternType.LITERAL),
                    new org.apache.kafka.common.acl.AccessControlEntry("User:admin", "127.0.0.1", AclOperation.DESCRIBE, AclPermissionType.ALLOW)
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
                AccessControlEntry producerWriteEntry = new AccessControlEntry("User:producer", "127.0.0.1", AclOperation.WRITE, AclPermissionType.ALLOW);
                AccessControlEntry producerDescribeEntry = new AccessControlEntry("User:producer", "127.0.0.1", AclOperation.DESCRIBE, AclPermissionType.ALLOW);
                AclBinding producerWriteAcl = new AclBinding(resourcePattern, producerWriteEntry);
                AclBinding producerDescribeAcl = new AclBinding(resourcePattern, producerDescribeEntry);
                aclBindings.add(producerWriteAcl);
                aclBindings.add(producerDescribeAcl);

                AclBindingFilter producerReadfilter = new AclBindingFilter(resourcePattern.toFilter(), producerWriteEntry.toFilter());
                AclBindingFilter producerDescribefilter = new AclBindingFilter(resourcePattern.toFilter(), producerDescribeEntry.toFilter());
                bindingFilters.add(producerReadfilter);
                bindingFilters.add(producerDescribefilter);

                int position = ipAddresses.indexOf(this.currentIP);
                // Create ACL entries for a consumer
                for (int index = 0; index < ipAddresses.size(); index++) {
                    String ip = ipAddresses.get(index);
                    if (ip.equals(this.currentIP) && !this.currentIP.equals("localhost"))
                        continue;
                    String host = this.currentIP.equals("localhost") ? "127.0.0.1" : ip;

                    //KafkaConfiguration.CONSUMER_PRIVATE_GROUP_ID + "-" + i + "-" + KafkaConfiguration.KAFKA_HOST
                    AccessControlEntry consumerReadEntry, consumerDescribeEntry;
                    if (topic.equals(TopicFactory.getInstance().getTopicName(TopicType.DISPERSE_PHASE1))) {
                        consumerReadEntry = new AccessControlEntry("User:consumer" + "-" + position + "-" + this.currentIP, host, AclOperation.READ, AclPermissionType.ALLOW);
                        consumerDescribeEntry = new AccessControlEntry("User:consumer" + "-" + position + "-" + this.currentIP, host, AclOperation.DESCRIBE, AclPermissionType.ALLOW);
                    } else {
                        consumerReadEntry = new AccessControlEntry("User:consumer" + "-" + index + "-" + ip, host, AclOperation.READ, AclPermissionType.ALLOW);
                        consumerDescribeEntry = new AccessControlEntry("User:consumer" + "-" + index + "-" + ip, host, AclOperation.DESCRIBE, AclPermissionType.ALLOW);
                    }
                    AclBinding consumerReadAcl = new AclBinding(resourcePattern, consumerReadEntry);
                    AclBinding consumerDescribeAcl = new AclBinding(resourcePattern, consumerDescribeEntry);
                    aclBindings.add(consumerReadAcl);
                    aclBindings.add(consumerDescribeAcl);

                    // Verify the ACLs
                    bindingFilters.add(consumerReadAcl.toFilter());
                    bindingFilters.add(consumerDescribeAcl.toFilter());
                    if (topic.equals(TopicFactory.getInstance().getTopicName(TopicType.DISPERSE_PHASE1))) {
                        ResourcePattern resourceSameGroupPattern = new ResourcePattern(ResourceType.GROUP, KafkaConfiguration.CONSUMER_SAME_GROUP_ID, PatternType.LITERAL);
                        AclBinding consumerSameGroupReadAcl = new AclBinding(resourceSameGroupPattern, consumerReadEntry);
                        AclBinding consumerSameGroupDescribeAcl = new AclBinding(resourceSameGroupPattern, consumerDescribeEntry);
                        aclBindings.add(consumerSameGroupReadAcl);
                        aclBindings.add(consumerSameGroupDescribeAcl);

                        // Verify the ACLs
                        bindingFilters.add(consumerSameGroupReadAcl.toFilter());
                        bindingFilters.add(consumerSameGroupDescribeAcl.toFilter());
                    } else {
                        int count = 0;
                        int local_position = ipAddresses.indexOf(ip);
                        for (int j = 0; j < ipAddresses.size(); j++) {
                            if (j == local_position && !this.currentIP.equals("localhost")) {
                                count++;
                                continue;
                            }
                            ResourcePattern resourcePrivateGroupPattern = new ResourcePattern(ResourceType.GROUP, KafkaConfiguration.CONSUMER_PRIVATE_GROUP_ID + "-" + count + "-" + ip, PatternType.LITERAL);
                            AclBinding consumerPrivateGroupReadAcl = new AclBinding(resourcePrivateGroupPattern, consumerReadEntry);
                            AclBinding consumerPrivateGroupDescribeAcl = new AclBinding(resourcePrivateGroupPattern, consumerDescribeEntry);
                            aclBindings.add(consumerPrivateGroupReadAcl);
                            aclBindings.add(consumerPrivateGroupDescribeAcl);

                            // Verify the ACLs
                            bindingFilters.add(consumerPrivateGroupReadAcl.toFilter());
                            bindingFilters.add(consumerPrivateGroupDescribeAcl.toFilter());
                            count++;
                        }
                    }

                }
            }


            adminClient.createAcls(aclBindings).all().get();
            System.out.println("ACLs created successfully");
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
        try {
            if (adminClient == null)
                return;
            // Delete the ACLs
            adminClient.deleteAcls(bindingFilters).all().get(2, TimeUnit.SECONDS);

//            // List all topics
//            Set<String> topics = adminClient.listTopics().names().get();
//
//            if(topics.isEmpty())
//                return;
//            // Delete all topics
//            DeleteTopicsResult deleteTopicsResult = adminClient.deleteTopics(topics);
//            deleteTopicsResult.all().get(4, TimeUnit.SECONDS);
            this.aclBindings.clear();
            this.bindingFilters.clear();
            this.configs.clear();
            this.ipAddresses.clear();
            this.props.clear();
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            LOG.error("Error while shutting down the KafkaCreatorTopic: {}", e.toString());
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


