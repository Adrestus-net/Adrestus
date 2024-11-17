package io.Adrestus.network;

import io.Adrestus.config.KafkaConfiguration;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.TopicListing;
import org.apache.kafka.common.acl.*;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourcePattern;
import org.apache.kafka.common.resource.ResourcePatternFilter;
import org.apache.kafka.common.resource.ResourceType;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class KafkaCreatorTopic implements IKafkaComponent {

    private Properties props;
    private final int DispersePartitionSize;
    private final String leader_host;
    private final ArrayList<String> ipAddresses;

    public KafkaCreatorTopic(ArrayList<String> ipAddresses, String leader_host, int DispersePartitionSize) {
        this.ipAddresses = ipAddresses;
        this.DispersePartitionSize = DispersePartitionSize;
        this.leader_host = leader_host;
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


        try (AdminClient adminClient = AdminClient.create(props)) {
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

            List<AclBinding> aclBindings = new ArrayList<>();
            List<AclBindingFilter> aclFilters = new ArrayList<>();
            for (NewTopic topic : TopicFactory.getInstance().getCollectionTopics()) {

                ResourcePattern resourcePattern = new ResourcePattern(ResourceType.TOPIC, topic.name(), PatternType.LITERAL);
                // Create ACL entries for a producer
                AccessControlEntry producerWriteEntry = new AccessControlEntry("User:producer", "localhost", AclOperation.WRITE, AclPermissionType.ALLOW);
                AccessControlEntry producerDescribeEntry = new AccessControlEntry("User:producer", "localhost", AclOperation.DESCRIBE, AclPermissionType.ALLOW);
                AclBinding producerWriteAcl = new AclBinding(resourcePattern, producerWriteEntry);
                AclBinding producerDescribeAcl = new AclBinding(resourcePattern, producerDescribeEntry);
                aclBindings.add(producerWriteAcl);
                aclBindings.add(producerDescribeAcl);

                // Create ACL entries for a consumer
                for (int index = 0; index < ipAddresses.size(); index++) {
                    String ip = ipAddresses.get(index);
                    if(ip.equals(leader_host))
                        continue;
                    AccessControlEntry consumerReadEntry = new AccessControlEntry("User:consumer" + "-" + index + "-" + ip, ip, AclOperation.READ, AclPermissionType.ALLOW);
                    AccessControlEntry consumerDescribeEntry = new AccessControlEntry("User:consumer" + "-" + index + "-" + ip, ip, AclOperation.DESCRIBE, AclPermissionType.ALLOW);
                    AclBinding consumerReadAcl = new AclBinding(resourcePattern, consumerReadEntry);
                    AclBinding consumerDescribeAcl = new AclBinding(resourcePattern, consumerDescribeEntry);
                    aclBindings.add(consumerReadAcl);
                    aclBindings.add(consumerDescribeAcl);
                    // Verify the ACLs
                    AclBindingFilter filter = new AclBindingFilter(new ResourcePatternFilter(ResourceType.TOPIC, TopicType.DISPERSE_PHASE1.name(), PatternType.LITERAL), consumerReadEntry.toFilter());
                    aclFilters.add(filter);
                }
            }
            adminClient.createAcls(aclBindings).all().get();
            aclFilters.stream().forEach(filter -> {
                //Print the ACLs
                //adminClient.describeAcls(filter).values().get().forEach(System.out::println);
            });
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
        if (props == null) {
            return;
        }

        //delete ACLS
        try (AdminClient adminClient = AdminClient.create(props)) {
            for (String topicName : TopicFactory.getInstance().getCollectionTopicsNames()) {
                ResourcePatternFilter resourceFilter = new ResourcePatternFilter(ResourceType.TOPIC, topicName, PatternType.LITERAL);
                AccessControlEntryFilter entryFilter = new AccessControlEntryFilter("User:producerUser", null, null, null);
                ArrayList<AclBindingFilter> bindingFilters = new ArrayList<>();
                AclBindingFilter filter = new AclBindingFilter(resourceFilter, entryFilter);
                bindingFilters.add(filter);
                for (String ip : ipAddresses) {
                    if(ip.equals(leader_host))
                        continue;
                    AccessControlEntryFilter entryFilterConsumer = new AccessControlEntryFilter("User:" + ip, null, null, null);
                    AclBindingFilter filterConsumer = new AclBindingFilter(resourceFilter, entryFilterConsumer);
                    bindingFilters.add(filterConsumer);
                }

                // Delete the ACLs
                adminClient.deleteAcls(bindingFilters).all().get();
                adminClient.deleteTopics(TopicFactory.getInstance().getAllCollectionTopicsNamesAsString()).all().get();
            }
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


