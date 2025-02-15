package io.Adrestus.network;

import com.google.common.reflect.TypeToken;
import io.Adrestus.config.KafkaConfiguration;
import io.Adrestus.rpc.CachedSerializableErasureObject;
import io.Adrestus.util.SerializationUtil;
import lombok.SneakyThrows;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetOutOfRangeException;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class ConsensusBroker {
    private static final Logger LOG = LoggerFactory.getLogger(ConsensusBroker.class);

    private final ReentrantReadWriteLock rwl;
    private final Lock r;
    private final Lock w;
    private final SerializationUtil<ArrayList<byte[]>> serenc_erasure;
    private final ArrayList<String> ipAddresses;
    private final KafkaSmith kafkaManufactureSmith;
    private final HashMap<TopicType, TreeMap<Integer, Map<Integer, byte[]>>> sequencedMap;
    private final ConcurrentHashMap<TopicType, Integer> timeoutMap;
    private final String currentIP;
    private final int numThreads;
    private final int partition;
    private final int currentPosition;
    private String leader_host;
    private int leader_position;

    public ConsensusBroker(ArrayList<String> ipAddresses, String leader_host, int partition) {
        this.rwl = new ReentrantReadWriteLock();
        this.r = rwl.readLock();
        this.w = rwl.writeLock();
        this.serenc_erasure = new SerializationUtil<ArrayList<byte[]>>(new TypeToken<List<byte[]>>() {
        }.getType());
        this.numThreads = Runtime.getRuntime().availableProcessors() * 2;
        this.currentIP = IPFinder.getLocalIP();
        this.ipAddresses = ipAddresses;
        this.leader_host = leader_host;
        this.currentPosition = this.ipAddresses.indexOf(this.currentIP);
        this.leader_position = this.ipAddresses.indexOf(leader_host);
        this.partition = partition;
        this.kafkaManufactureSmith = new KafkaManufactureSmith(ipAddresses, leader_host, this.currentIP, partition);
        this.sequencedMap = new HashMap<TopicType, TreeMap<Integer, Map<Integer, byte[]>>>();
        this.timeoutMap = new ConcurrentHashMap<>();
        this.Init();
    }

    private void Init() {
        this.sequencedMap.put(TopicType.ANNOUNCE_PHASE, new TreeMap<>(Collections.reverseOrder()));
        this.sequencedMap.put(TopicType.PREPARE_PHASE, new TreeMap<>(Collections.reverseOrder()));
        this.sequencedMap.put(TopicType.COMMITTEE_PHASE, new TreeMap<>(Collections.reverseOrder()));
        this.sequencedMap.put(TopicType.DISPERSE_PHASE1, new TreeMap<>(Collections.reverseOrder()));
        this.sequencedMap.put(TopicType.DISPERSE_PHASE2, new TreeMap<>(Collections.reverseOrder()));

        this.timeoutMap.put(TopicType.ANNOUNCE_PHASE, KafkaConfiguration.ANNOUNCE_PHASE_RECEIVE_TIMEOUT);
        this.timeoutMap.put(TopicType.PREPARE_PHASE, KafkaConfiguration.PREPARE_PHASE_RECEIVE_TIMEOUT);
        this.timeoutMap.put(TopicType.COMMITTEE_PHASE, KafkaConfiguration.COMMITTEE_PHASE_RECEIVE_TIMEOUT);
        this.timeoutMap.put(TopicType.DISPERSE_PHASE2, KafkaConfiguration.DISPERSE_PHASE2_RECEIVE_TIMEOUT);
        this.timeoutMap.put(TopicType.DISPERSE_PHASE1, KafkaConfiguration.DISPERSE_PHASE1_RECEIVE_TIMEOUT);
    }

    @SneakyThrows
    public void initializeKafkaKingdom() {
        this.kafkaManufactureSmith.manufactureKafkaComponent(KafkaKingdomType.ZOOKEEPER);
        this.kafkaManufactureSmith.manufactureKafkaComponent(KafkaKingdomType.BROKER);
        this.kafkaManufactureSmith.manufactureKafkaComponent(KafkaKingdomType.TOPIC_CREATOR);
        this.kafkaManufactureSmith.manufactureKafkaComponent(KafkaKingdomType.CONSUMER_PRIVATE);
        this.kafkaManufactureSmith.manufactureKafkaComponent(KafkaKingdomType.CONSUMER_SAME);
        this.kafkaManufactureSmith.manufactureKafkaComponent(KafkaKingdomType.PRODUCER);
        this.setupChannels();
        LOG.info("Kafka Kingdom initialized successfully");
    }


    private void setupChannels() throws InterruptedException {
        for (int i = 0; i < this.ipAddresses.size(); i++) {
            if (this.ipAddresses.get(i).equals(this.currentIP))
                continue;
            this.produceMessage(TopicType.DISPERSE_PHASE1, i, "0", "0".getBytes(StandardCharsets.UTF_8));
        }
        this.flush();
        this.BootstrapReceiveDisperseMessageFromAll(TopicType.DISPERSE_PHASE1, "0");
        if (currentIP.equals(leader_host)) {
            this.receiveMessageFromValidators(TopicType.DISPERSE_PHASE2, "0");
            this.produceMessage(TopicType.ANNOUNCE_PHASE, "0", "0".getBytes(StandardCharsets.UTF_8));
            this.receiveMessageFromValidators(TopicType.ANNOUNCE_PHASE, "0");
            this.produceMessage(TopicType.PREPARE_PHASE, "0", "0".getBytes());
            this.receiveMessageFromValidators(TopicType.PREPARE_PHASE, "0");
            this.produceMessage(TopicType.COMMITTEE_PHASE, "0", "0".getBytes(StandardCharsets.UTF_8));
            this.receiveMessageFromValidators(TopicType.COMMITTEE_PHASE, "0");
        } else {
            this.produceMessage(TopicType.DISPERSE_PHASE2, "0", "0".getBytes(StandardCharsets.UTF_8));
            this.receiveMessageFromValidators(TopicType.DISPERSE_PHASE2, "0");
            this.receiveMessageFromLeader(TopicType.ANNOUNCE_PHASE, "0");
            this.produceMessage(TopicType.ANNOUNCE_PHASE, "0", "0".getBytes(StandardCharsets.UTF_8));
            this.receiveMessageFromLeader(TopicType.PREPARE_PHASE, "0");
            this.produceMessage(TopicType.PREPARE_PHASE, "0", "0".getBytes(StandardCharsets.UTF_8));
            this.receiveMessageFromLeader(TopicType.COMMITTEE_PHASE, "0");
            this.produceMessage(TopicType.COMMITTEE_PHASE, "0", "0".getBytes(StandardCharsets.UTF_8));
            Thread.sleep(500);
            this.seekAllOffsetToEnd();
        }
    }

    private void add(TopicType current, ConsumerRecord<String, byte[]> record, int finalI) {
        w.lock();
        try {
            this.sequencedMap.get(current).get(Integer.parseInt(record.key())).put(finalI, record.value());
        } finally {
            w.unlock();
        }
    }

    public void distributeDisperseMessageFromLeader(ArrayList<ArrayList<byte[]>> data, String key) {
        if (data.size() != this.ipAddresses.size() - 1)
            throw new IllegalArgumentException("DisperseMessage Invalid data size");


        int j = 0;
        for (int i = 0; i < this.ipAddresses.size(); i++) {
            if (this.ipAddresses.get(i).equals(this.currentIP))
                continue;
            ArrayList<byte[]> toSend = data.get(j);
            int sum = toSend.parallelStream().mapToInt(byteArray -> byteArray.length).sum() + 1024 * 6;
            byte[] toSendBytes = this.serenc_erasure.encode(toSend, sum);
            CachedSerializableErasureObject.getInstance().setSerializableErasureObject(this.ipAddresses.get(i), toSendBytes);
            this.produceMessage(TopicType.DISPERSE_PHASE1, i, key + i, toSendBytes);
            j++;
        }

        this.flush();
    }

    public void distributeDisperseMessageToValidators(ArrayList<byte[]> data, String key) {
        if (data.isEmpty())
            throw new IllegalArgumentException("DisperseMessageToValidators Invalid data size");

        this.produceMessage(TopicType.DISPERSE_PHASE2, key, this.serenc_erasure.encode(data, data.parallelStream().mapToInt(byteArray -> byteArray.length).sum() + 1024 * 6));
        this.flush();
    }

    public HashMap<String, ArrayList<byte[]>> retrieveDisperseMessageFromValidatorsAndConcatResponse(ArrayList<byte[]> leader_data, String key) {
        HashMap<Integer, byte[]> response = (HashMap<Integer, byte[]>) this.receiveDisperseMessageFromValidators(TopicType.DISPERSE_PHASE2, key);
        if (response.isEmpty())
            throw new IllegalArgumentException("DisperseMessageFromValidators Invalid data size");


        HashMap<String, ArrayList<byte[]>> concatResponse = new HashMap<>();
        for (Map.Entry<Integer, byte[]> entry : response.entrySet()) {
            concatResponse.put(this.ipAddresses.get(entry.getKey()), new ArrayList<>(this.serenc_erasure.decode(entry.getValue())));
        }

        concatResponse.put(this.leader_host, leader_data);
        return concatResponse;
    }

    public void produceMessage(TopicType topic, String key, byte[] data) {
        KafkaProducer producer = this.kafkaManufactureSmith.getKafkaComponent(KafkaKingdomType.PRODUCER);
        producer.produceMessage(topic.name(), key, data);

    }

    public void produceMessage(TopicType topic, int partition, String key, byte[] data) {
        KafkaProducer producer = this.kafkaManufactureSmith.getKafkaComponent(KafkaKingdomType.PRODUCER);
        producer.produceMessage(topic.name(), partition, key, data);
    }

    public void flush() {
        KafkaProducer producer = this.kafkaManufactureSmith.getKafkaComponent(KafkaKingdomType.PRODUCER);
        producer.getProducer().flush();
    }

    public void seekDisperseOffsetToEnd() {
        KafkaConsumerPrivateGroup leaderConsumeData = this.kafkaManufactureSmith.getKafkaComponent(KafkaKingdomType.CONSUMER_PRIVATE);
        List<Consumer<String, byte[]>> validators = leaderConsumeData.receiveAllBrokerConsumersExceptLeader(leader_host);
        validators.forEach(validator -> {
            Map<TopicPartition, Long> endOffsets = validator.endOffsets(validator.assignment());
            TopicPartition partition = endOffsets.keySet().stream().filter(topicPartition -> topicPartition.topic().equals(TopicType.DISPERSE_PHASE2.name())).findFirst().get();
            validator.seek(partition, endOffsets.get(partition).intValue() + 1);
            validator.position(partition);
        });
    }

    public void seekAllOffsetToEnd() {
        KafkaConsumerPrivateGroup leaderConsumeData = this.kafkaManufactureSmith.getKafkaComponent(KafkaKingdomType.CONSUMER_PRIVATE);
        List<Consumer<String, byte[]>> validators = leaderConsumeData.receiveAllBrokerConsumersExceptLeader(leader_host);
        validators.forEach(validator -> {
            Map<TopicPartition, Long> endOffsets = validator.endOffsets(validator.assignment());
            endOffsets.forEach((topicPartition, offset) -> {
                validator.seek(topicPartition, offset);
                validator.position(topicPartition);
            });
        });
    }

    @SneakyThrows
    public Map<Integer, byte[]> receiveDisperseMessageFromValidators(TopicType topic, String key) {
        KafkaConsumerPrivateGroup leaderConsumeData = this.kafkaManufactureSmith.getKafkaComponent(KafkaKingdomType.CONSUMER_PRIVATE);
        Map<String, Consumer<String, byte[]>> consumers = leaderConsumeData.receiveAllMapBrokerConsumersExceptLeader(leader_host);
        int size = consumers.size();

        if (this.sequencedMap.get(topic).containsKey(Integer.parseInt(key))) {
            if (this.sequencedMap.get(topic).get(Integer.parseInt(key)).size() == size) {
                LOG.info(Thread.currentThread().getStackTrace()[1].getMethodName() + " - " + "Returning from cache: " + topic.name() + " " + key);
                return this.sequencedMap.get(topic).get(Integer.parseInt(key));
            } else {
                size = size - this.sequencedMap.get(topic).get(Integer.parseInt(key)).size();
            }
        }

        ExecutorService executorService = Executors.newFixedThreadPool(this.numThreads);
        CountDownLatch await_latch = new CountDownLatch(size);
        for (Map.Entry<String, Consumer<String, byte[]>> entry : consumers.entrySet()) {
            Runnable task = () -> {
                int timeout = 0;
                boolean flag = false;
                while (timeout < KafkaConfiguration.EXECUTOR_TIMEOUT) {
                    try {
                        Consumer<String, byte[]> consumer = entry.getValue();
                        ConsumerRecords<String, byte[]> records = consumer.poll(Duration.ofMillis(this.timeoutMap.get(topic)));
                        LOG.info("{} {} {} {}", "receiveDisperseMessageFromValidators", Thread.currentThread().getName(), topic.name(), records.count());
                        for (ConsumerRecord<String, byte[]> record : records) {
                            TopicType current = TopicType.valueOf(record.topic());
                            LOG.info("{} {} {} {} {}", "receiveDisperseMessageFromValidators", Thread.currentThread().getName(), current, "Key: " + record.key(), "Offset: " + record.offset() + "finalI: " + entry.getKey());
                            w.lock();
                            try {
                                if (record.key().equals(key) && record.topic().equals(topic.name())) {
                                    if (!this.sequencedMap.get(current).containsKey(Integer.parseInt(record.key()))) {
                                        this.sequencedMap.get(current).put(Integer.parseInt(record.key()), new HashMap<>());
                                    }
//                                if (this.sequencedMap.get(current).containsKey(Integer.parseInt(record.key())) && !current.equals(TopicType.DISPERSE_PHASE2) && !currentIP.equals(leader_host)) {
//                                    LOG.info("{} {} {} {} {}", "Duplicate-receiveDisperseMessageFromValidators", Thread.currentThread().getName(), current, "Key: " + record.key(), "Offset: " + record.offset());
//                                }
                                    add(current, record, this.ipAddresses.indexOf(entry.getKey()));
                                    flag = true;
                                }
                            } finally {
                                w.unlock();
                            }
                        }
                        if (flag) {
                            await_latch.countDown();
                            break;
                        }
                        timeout++;
                    } catch (OffsetOutOfRangeException e) {
                        // LOG.info("Offset out of range for topic: " + topic.name());
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new IllegalArgumentException("Error in receiving message from validators" + e.getMessage());
                    }
                }
                if (!flag)
                    await_latch.countDown();
                LOG.info("");
            };
            executorService.submit(task);
        }
        await_latch.await();

        Thread.ofVirtual().start(() -> {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(100, TimeUnit.MILLISECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
            }
            executorService.close();
        });
        return this.sequencedMap.get(topic).firstEntry().getValue();
    }

    @SneakyThrows
    public List<byte[]> receiveMessageFromValidators(TopicType topic, String key) {
        KafkaConsumerPrivateGroup leaderConsumeData = this.kafkaManufactureSmith.getKafkaComponent(KafkaKingdomType.CONSUMER_PRIVATE);
        Map<String, Consumer<String, byte[]>> consumers = leaderConsumeData.receiveAllMapBrokerConsumersExceptLeader(leader_host);
        int size = consumers.size();

        if (this.sequencedMap.get(topic).containsKey(Integer.parseInt(key))) {
            if (this.sequencedMap.get(topic).get(Integer.parseInt(key)).size() == size) {
                LOG.info(Thread.currentThread().getStackTrace()[1].getMethodName() + " - " + "Returning from cache: " + topic.name() + " " + key);
                return new ArrayList<>(this.sequencedMap.get(topic).get(Integer.parseInt(key)).values());
            } else {
                size = size - this.sequencedMap.get(topic).get(Integer.parseInt(key)).size();
            }
        }

        ExecutorService executorService = Executors.newFixedThreadPool(this.numThreads);
        CountDownLatch await_latch = new CountDownLatch(size);
        for (Map.Entry<String, Consumer<String, byte[]>> entry : consumers.entrySet()) {
            Runnable task = () -> {
                int timeout = 0;
                boolean flag = false;
                while (timeout < KafkaConfiguration.EXECUTOR_TIMEOUT) {
                    try {
                        Consumer<String, byte[]> consumer = entry.getValue();
                        ConsumerRecords<String, byte[]> records = consumer.poll(Duration.ofMillis(this.timeoutMap.get(topic)));
                        LOG.info("{} {} {} {}", "receiveMessageFromValidators", Thread.currentThread().getName(), topic.name(), records.count());
                        for (ConsumerRecord<String, byte[]> record : records) {
                            TopicType current = TopicType.valueOf(record.topic());
                            LOG.info("{} {} {} {} {}", "receiveMessageFromValidators", Thread.currentThread().getName(), current, "Key: " + record.key(), "Offset: " + record.offset() + "finalI: " + this.ipAddresses.indexOf(entry.getKey()));
                            w.lock();
                            try {
                                if (!this.sequencedMap.get(current).containsKey(Integer.parseInt(record.key()))) {
                                    this.sequencedMap.get(current).put(Integer.parseInt(record.key()), new HashMap<>());
                                }
                                if (this.sequencedMap.get(current).containsKey(Integer.parseInt(record.key())) && !current.equals(TopicType.DISPERSE_PHASE2) && !currentIP.equals(leader_host)) {
                                    LOG.info("{} {} {} {} {}", "Duplicate-receiveMessageFromValidators", Thread.currentThread().getName(), current, "Key: " + record.key(), "Offset: " + record.offset());
                                }
                            } finally {
                                w.unlock();
                            }
                            add(current, record, this.ipAddresses.indexOf(entry.getKey()));
                            if (record.key().equals(key) && record.topic().equals(topic.name())) {
                                flag = true;
                            }
                        }
                        if (flag) {
                            await_latch.countDown();
                            break;
                        }
                        timeout++;
                    } catch (OffsetOutOfRangeException e) {
                        // LOG.info("Offset out of range for topic: " + topic.name());
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new IllegalArgumentException("Error in receiving message from validators" + e.getMessage());
                    }
                }
                if (!flag)
                    await_latch.countDown();
                LOG.info("");
            };
            executorService.submit(task);
        }
        await_latch.await();

        Thread.ofVirtual().start(() -> {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(100, TimeUnit.MILLISECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
            }
            executorService.close();
        });
        return this.sequencedMap.get(topic).firstEntry().getValue().values().stream().collect(Collectors.toList());
    }

    @SneakyThrows
    public Optional<byte[]> receiveMessageFromLeader(TopicType topic, String key) {
        if (this.sequencedMap.get(topic).containsKey(Integer.parseInt(key))) {
            if (this.sequencedMap.get(topic).firstEntry().getValue().containsKey(this.ipAddresses.indexOf(leader_host))) {
                LOG.info(Thread.currentThread().getStackTrace()[1].getMethodName() + " - " + "Returning from cache: " + topic.name() + " " + key);
                return Optional.of(this.sequencedMap.get(topic).firstEntry().getValue().get(this.ipAddresses.indexOf(leader_host)));
            }
        }

        KafkaConsumerPrivateGroup leaderConsumeData = this.kafkaManufactureSmith.getKafkaComponent(KafkaKingdomType.CONSUMER_PRIVATE);
        ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
        Consumer<String, byte[]> consumer = leaderConsumeData.receiveLeaderConsumer(leader_host);
        CountDownLatch await_latch = new CountDownLatch(1);
        Runnable task = () -> {
            int timeout = 0;
            boolean flag = false;
            while (timeout < KafkaConfiguration.EXECUTOR_TIMEOUT) {
                try {
                    ConsumerRecords<String, byte[]> records = consumer.poll(Duration.ofMillis(this.timeoutMap.get(topic)));
                    LOG.info("{} {} {} {}", "receiveMessageFromLeader", Thread.currentThread().getName(), topic.name(), records.count());
                    for (ConsumerRecord<String, byte[]> record : records) {
                        TopicType current = TopicType.valueOf(record.topic());
                        LOG.info("{} {} {} {} {}", "receiveMessageFromLeader", Thread.currentThread().getName(), current, "Key: " + record.key(), "Offset: " + record.offset());
                        try {
                            w.lock();
                            if (!this.sequencedMap.get(current).containsKey(Integer.parseInt(record.key()))) {
                                this.sequencedMap.get(current).put(Integer.parseInt(record.key()), new HashMap<>());
                            }
                            add(current, record, 0);
                            if (record.key().equals(key) && record.topic().equals(topic.name())) {
                                flag = true;
                            }
                        } finally {
                            w.unlock();
                        }
                    }
                    if (flag) {
                        await_latch.countDown();
                        break;
                    }
                    timeout++;
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new IllegalArgumentException("Error in receiving message from leader" + e.getMessage());
                }
            }
            if (!flag)
                await_latch.countDown();
            LOG.info("");
        };
        executorService.submit(task);
        await_latch.await();
        Thread.ofVirtual().start(() -> {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(100, TimeUnit.MILLISECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
            }
            executorService.close();
        });
        return this.sequencedMap.get(topic).firstEntry().getValue().values().stream().findFirst();
    }

    @SneakyThrows
    public ArrayList<byte[]> receiveDisperseHandledMessageFromLeader(TopicType topic, String key) {
        Optional<byte[]> message = this.receiveDisperseMessageFromLeader(topic, key);
        if (message.isEmpty())
            throw new IllegalArgumentException("DisperseMessage not received correctly form leader aborting");

        return new ArrayList<>(this.serenc_erasure.decode(message.get()));
    }

    @SneakyThrows
    public Optional<byte[]> BootstrapReceiveDisperseMessageFromAll(TopicType topic, String key) {
        KafkaConsumerSameGroup consumer_data = this.kafkaManufactureSmith.getKafkaComponent(KafkaKingdomType.CONSUMER_SAME);
        List<Consumer<String, byte[]>> consumers = consumer_data.getConsumer_map().values().stream().collect(Collectors.toList());
        int size = consumers.size();
        ExecutorService executorService = Executors.newFixedThreadPool(this.numThreads);
        CountDownLatch await_latch = new CountDownLatch(size);
        for (int i = 0; i < size; i++) {
            int finalI = i;
            Runnable task = () -> {
                int timeout = 0;
                boolean flag = false;
                while (timeout < KafkaConfiguration.EXECUTOR_TIMEOUT) {
                    try {
                        Consumer<String, byte[]> consumer = consumers.get(finalI);
                        ConsumerRecords<String, byte[]> records = consumer.poll(Duration.ofMillis(this.timeoutMap.get(topic)));
                        LOG.info("{} {} {} {}", "receiveMessageFromValidators", Thread.currentThread().getName(), topic.name(), records.count());
                        for (ConsumerRecord<String, byte[]> record : records) {
                            TopicType current = TopicType.valueOf(record.topic());
                            LOG.info("{} {} {} {} {}", "receiveMessageFromValidators", Thread.currentThread().getName(), current, "Key: " + record.key(), "Offset: " + record.offset());
                            w.lock();
                            try {
                                if (record.key().equals(key) && record.topic().equals(topic.name())) {
                                    if (!this.sequencedMap.get(current).containsKey(Integer.parseInt(record.key()))) {
                                        this.sequencedMap.get(current).put(Integer.parseInt(record.key()), new HashMap<>());
                                    }
                                    add(current, record, finalI);
                                    flag = true;
                                }
                            } finally {
                                w.unlock();
                            }
                        }
                        if (flag) {
                            await_latch.countDown();
                            break;
                        }
                        timeout++;
                    } catch (OffsetOutOfRangeException e) {
                        // LOG.info("Offset out of range for topic: " + topic.name());
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new IllegalArgumentException("Error in receiving message from validators" + e.getMessage());
                    }
                }
                if (!flag)
                    await_latch.countDown();
                LOG.info("");
            };
            executorService.submit(task);
        }
        await_latch.await();

        Thread.ofVirtual().start(() -> {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(100, TimeUnit.MILLISECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
            }
            executorService.close();
        });
        if (!this.sequencedMap.get(TopicType.DISPERSE_PHASE1).containsKey(Integer.parseInt(key)))
            throw new IllegalArgumentException("DisperseMessage1 not received correctly form leader aborting");
        if (this.sequencedMap.get(TopicType.DISPERSE_PHASE1).get(Integer.parseInt(key)).size() != size)
            throw new IllegalArgumentException("DisperseMessage1 size is wrong and not received correctly form all leaders aborting");

        return Optional.empty();
    }

    @SneakyThrows
    public Optional<byte[]> receiveDisperseMessageFromLeader(TopicType topic, String key) {
        if (this.sequencedMap.get(topic).containsKey(Integer.parseInt(key))) {
            LOG.info(Thread.currentThread().getStackTrace()[1].getMethodName() + " - " + "Returning from cache: " + topic.name() + " " + key);
            return this.sequencedMap.get(topic).firstEntry().getValue().values().stream().findFirst();
        }

        KafkaConsumerSameGroup leaderSameConsumeData = this.kafkaManufactureSmith.getKafkaComponent(KafkaKingdomType.CONSUMER_SAME);
        ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
        Consumer<String, byte[]> consumer = leaderSameConsumeData.receiveLeaderConsumer(leader_host);
        int position = leaderSameConsumeData.getPositionOfElement(leader_host).get();
        CountDownLatch await_latch = new CountDownLatch(1);
        Runnable task = () -> {
            int timeout = 0;
            boolean flag = false;
            outerloop:
            while (timeout < KafkaConfiguration.EXECUTOR_TIMEOUT) {
                try {
                    ConsumerRecords<String, byte[]> records = consumer.poll(Duration.ofMillis(this.timeoutMap.get(topic)));
                    LOG.info("{} {} {} {}", "receiveDisperseMessageFromLeader", Thread.currentThread().getName(), topic.name(), records.count());
                    for (ConsumerRecord<String, byte[]> record : records) {
                        TopicType current = TopicType.valueOf(record.topic());
                        LOG.info("{} {} {} {} {} {}", "receiveDisperseMessageFromLeader", Thread.currentThread().getName(), current, "Key: " + record.key(), "Offset: " + record.offset(), "partition: " + record.partition());
                        w.lock();
                        try {
                            if (!this.sequencedMap.get(current).containsKey(Integer.parseInt(record.key()))) {
                                this.sequencedMap.get(current).put(Integer.parseInt(record.key()), new HashMap<>());
                            }
                        } finally {
                            w.unlock();
                        }
                        add(current, record, position);
                        if (record.key().equals(key) && record.topic().equals(topic.name())) {
                            flag = true;
                            await_latch.countDown();
                            break outerloop;
                        }
                    }
                    timeout++;
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new IllegalArgumentException("Error in receiving disperse message from leader" + e.getMessage());
                }
            }
            LOG.info("");
            if (!flag)
                await_latch.countDown();
        };
        executorService.submit(task);
        await_latch.await();
        Thread.ofVirtual().start(() -> {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(100, TimeUnit.MILLISECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
            }
            executorService.close();
        });
        return this.sequencedMap.get(topic).firstEntry().getValue().values().stream().findFirst();
    }

    public void clear() {
        this.sequencedMap.forEach((key, value) -> {
            value.forEach((k, v) -> v.clear());
        });
    }

    public void shutDownGracefully() {
        this.kafkaManufactureSmith.shutDownGracefully();
        this.ipAddresses.clear();
        this.sequencedMap.forEach((key, value) -> {
            value.forEach((k, v) -> v.clear());
            value.clear();
        });
        this.sequencedMap.clear();
        this.timeoutMap.clear();
    }

    public void setLeader_host(String leader_host) {
        this.leader_host = leader_host;
    }

    public void setLeader_position(int leader_position) {
        this.leader_position = leader_position;
    }

    public ArrayList<String> getIpAddresses() {
        return ipAddresses;
    }

    public KafkaSmith getKafkaManufactureSmith() {
        return kafkaManufactureSmith;
    }

    public String getLeader_host() {
        return leader_host;
    }

    public int getPartition() {
        return partition;
    }

    public int getLeader_position() {
        return leader_position;
    }

    public int getNumThreads() {
        return numThreads;
    }

    public String getCurrentIP() {
        return currentIP;
    }

    public HashMap<TopicType, TreeMap<Integer, Map<Integer, byte[]>>> getSequencedMap() {
        return sequencedMap;
    }

    public SerializationUtil<ArrayList<byte[]>> getSerenc_erasure() {
        return serenc_erasure;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConsensusBroker that = (ConsensusBroker) o;
        return partition == that.partition && Objects.equals(ipAddresses, that.ipAddresses) && Objects.equals(kafkaManufactureSmith, that.kafkaManufactureSmith) && Objects.equals(leader_host, that.leader_host);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ipAddresses, kafkaManufactureSmith, leader_host, partition);
    }

    @Override
    public String toString() {
        return "ConsensusBroker{" +
                "ipAddresses=" + ipAddresses +
                ", kafkaManufactureSmith=" + kafkaManufactureSmith +
                ", leader_host='" + leader_host + '\'' +
                ", partition=" + partition +
                '}';
    }
}

