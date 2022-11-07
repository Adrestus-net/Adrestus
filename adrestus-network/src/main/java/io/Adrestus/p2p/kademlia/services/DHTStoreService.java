package io.Adrestus.p2p.kademlia.services;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import io.Adrestus.p2p.kademlia.connection.ConnectionInfo;
import io.Adrestus.p2p.kademlia.exception.DuplicateStoreRequest;
import io.Adrestus.p2p.kademlia.model.FindNodeAnswer;
import io.Adrestus.p2p.kademlia.model.StoreAnswer;
import io.Adrestus.p2p.kademlia.node.DHTKademliaNodeAPI;
import io.Adrestus.p2p.kademlia.node.KademliaNodeAPI;
import io.Adrestus.p2p.kademlia.node.Node;
import io.Adrestus.p2p.kademlia.node.external.ExternalNode;
import io.Adrestus.p2p.kademlia.protocol.MessageType;
import io.Adrestus.p2p.kademlia.protocol.handler.MessageHandler;
import io.Adrestus.p2p.kademlia.protocol.message.DHTStoreKademliaMessage;
import io.Adrestus.p2p.kademlia.protocol.message.DHTStoreResultKademliaMessage;
import io.Adrestus.p2p.kademlia.protocol.message.EmptyKademliaMessage;
import io.Adrestus.p2p.kademlia.protocol.message.KademliaMessage;
import io.Adrestus.p2p.kademlia.util.DateUtil;
import io.Adrestus.p2p.kademlia.util.NodeUtil;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;


public class DHTStoreService<ID extends Number, C extends ConnectionInfo, K extends Serializable, V extends Serializable> implements MessageHandler<ID, C> {
    private final DHTKademliaNodeAPI<ID, C, K, V> dhtKademliaNode;
    private final ListeningExecutorService listeningExecutorService;
    private final ExecutorService cleanupExecutor;
    private final ExecutorService handlerExecutorService;
    private final Map<K, StoreAnswer<ID, K>> storeMap = new ConcurrentHashMap<>();

    public DHTStoreService(
            DHTKademliaNodeAPI<ID, C, K, V> dhtKademliaNode,
            ExecutorService executorService,
            ExecutorService cleanupExecutor
    ) {
        this.dhtKademliaNode = dhtKademliaNode;
        this.listeningExecutorService = (executorService instanceof ListeningExecutorService) ? (ListeningExecutorService) executorService : MoreExecutors.listeningDecorator(executorService);
        this.cleanupExecutor = cleanupExecutor;
        this.handlerExecutorService = executorService;
    }

    public Future<StoreAnswer<ID, K>> store(K key, V value) throws DuplicateStoreRequest {
        // Todo: compute if absent

        if (storeMap.containsKey(key)) {
            throw new DuplicateStoreRequest();
        }


        ListenableFuture<StoreAnswer<ID, K>> futureAnswer = this.listeningExecutorService.submit(
                () -> {
                    StoreAnswer<ID, K> storeAnswer = handleStore(this.dhtKademliaNode, this.dhtKademliaNode, key, value);
                    // If immediately failed or stored, then return, otherwise watch storeAnswer
                    if (storeAnswer.getResult().equals(StoreAnswer.Result.STORED) || storeAnswer.getResult().equals(StoreAnswer.Result.FAILED)) {
                        return storeAnswer;
                    }
                    storeMap.put(key, storeAnswer);
                    storeAnswer.watch();
                    return storeAnswer;
                }
        );

        futureAnswer.addListener(() -> {
            StoreAnswer<ID, K> storeAnswer = storeMap.remove(key);
            if (storeAnswer != null) {
                storeAnswer.finishWatch();
            }
        }, this.cleanupExecutor);

        return futureAnswer;
    }

    public void cleanUp() {
        this.storeMap.forEach((k, idkStoreAnswer) -> idkStoreAnswer.finishWatch());
        this.storeMap.clear();
    }

    protected StoreAnswer<ID, K> handleStore(Node<ID, C> caller, Node<ID, C> requester, K key, V value) {
        StoreAnswer<ID, K> storeAnswer;
        ID hash = this.dhtKademliaNode.getKeyHashGenerator().generateHash(key);

        // If some other node is calling the store, and that other node is not this node,
        // But the origin request is by this node, then persist it
        // The closest node we know to the key knows us as the closest know to the key and not themselves (?!?)
        // Useful only in case of nodeSettings.isEnabledFirstStoreRequestForcePass()
        if (!caller.getId().equals(this.dhtKademliaNode.getId()) && requester.getId().equals(this.dhtKademliaNode.getId())) {
            return doStore(key, value);
        }

        // If current node should persist the data, do it immediately
        // For smaller networks this helps to avoid the process of finding alive close nodes to pass data to

        FindNodeAnswer<ID, C> findNodeAnswer = this.dhtKademliaNode.getRoutingTable().findClosest(hash);
        storeAnswer = storeDataToClosestNode(caller, requester, findNodeAnswer.getNodes(), key, value);


        if (storeAnswer.getResult().equals(StoreAnswer.Result.FAILED)) {
            storeAnswer = doStore(key, value);
        }
        return storeAnswer;
    }

    protected StoreAnswer<ID, K> doStore(K key, V value) {
        this.dhtKademliaNode.getKademliaRepository().store(key, value);
        return getNewStoreAnswer(key, StoreAnswer.Result.STORED, this.dhtKademliaNode);
    }

    protected StoreAnswer<ID, K> storeDataToClosestNode(Node<ID, C> caller, Node<ID, C> requester, List<ExternalNode<ID, C>> externalNodeList, K key, V value) {
        Date date = DateUtil.getDateOfSecondsAgo(this.dhtKademliaNode.getNodeSettings().getMaximumLastSeenAgeToConsiderAlive());
        for (ExternalNode<ID, C> externalNode : externalNodeList) {
            //if current node is the closest node, store the value (Scenario A)
            if (externalNode.getId().equals(this.dhtKademliaNode.getId())) {
                this.dhtKademliaNode.getKademliaRepository().store(key, value);
                return getNewStoreAnswer(key, StoreAnswer.Result.STORED, this.dhtKademliaNode);
            }

            // Continue if requester is known to be the closest, but it's also same as caller
            // This means that this is the first time that PASS is happening and requester wants for force pushing it to other nodes,
            // or in other words:
            // This is the first time the requester node has passed the store request to some other node. So we try more.
            // This approach can be disabled through nodeSettings "Enabled First Store Request Force Pass"
            // This has no conflicts with 'Scenario A' because:
            // If we were the closest node we'd have already stored the data

            if (requester.getId().equals(externalNode.getId()) && requester.getId().equals(caller.getId())
                    && this.dhtKademliaNode.getNodeSettings().isEnabledFirstStoreRequestForcePass()
            ) {
                continue;
            }

            // otherwise, try next closest node in routing table
            // if close node is alive, tell it to store the data
            // to know if it's alive the last seen should either be close or we ping and check the result
            if (NodeUtil.recentlySeenOrAlive(this.dhtKademliaNode, externalNode, date)) {
                KademliaMessage<ID, C, Serializable> response = this.dhtKademliaNode.getMessageSender().sendMessage(
                        this.dhtKademliaNode,
                        externalNode,
                        new DHTStoreKademliaMessage<>(
                                new DHTStoreKademliaMessage.DHTData<>(requester, key, value)
                        )
                );
                if (response.isAlive()) {
                    return getNewStoreAnswer(key, StoreAnswer.Result.PASSED, requester);
                }
            }

        }
        return getNewStoreAnswer(key, StoreAnswer.Result.FAILED, requester);
    }


    protected EmptyKademliaMessage<ID, C> handleStoreResult(DHTStoreResultKademliaMessage<ID, C, K> message) {
        DHTStoreResultKademliaMessage.DHTStoreResult<K> data = message.getData();
        StoreAnswer<ID, K> storeAnswer = this.storeMap.get(data.getKey());
        if (storeAnswer != null) {
            storeAnswer.setNodeId(message.getNode().getId());
            storeAnswer.setResult(data.getResult());
            storeAnswer.setAlive(true);
            storeAnswer.finishWatch();
        }
        return new EmptyKademliaMessage<>();
    }

    protected EmptyKademliaMessage<ID, C> handleStoreRequest(DHTStoreKademliaMessage<ID, C, K, V> dhtStoreKademliaMessage) {
        this.handlerExecutorService.submit(() -> {
            DHTStoreKademliaMessage.DHTData<ID, C, K, V> data = dhtStoreKademliaMessage.getData();
            StoreAnswer<ID, K> storeAnswer = handleStore(dhtStoreKademliaMessage.getNode(), data.getRequester(), data.getKey(), data.getValue());
            if (storeAnswer.getResult().equals(StoreAnswer.Result.STORED)) {
                this.dhtKademliaNode.getMessageSender().sendAsyncMessage(
                        this.dhtKademliaNode,
                        data.getRequester(),
                        new DHTStoreResultKademliaMessage<>(
                                new DHTStoreResultKademliaMessage.DHTStoreResult<>(data.getKey(), StoreAnswer.Result.STORED)
                        )
                );
            }
        });
        return new EmptyKademliaMessage<>();
    }


    protected StoreAnswer<ID, K> getNewStoreAnswer(K k, StoreAnswer.Result result, Node<ID, C> node) {
        StoreAnswer<ID, K> storeAnswer = new StoreAnswer<>();
        storeAnswer.setAlive(true);
        storeAnswer.setNodeId(node.getId());
        storeAnswer.setKey(k);
        storeAnswer.setResult(result);
        return storeAnswer;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <I extends KademliaMessage<ID, C, ?>, O extends KademliaMessage<ID, C, ?>> O handle(KademliaNodeAPI<ID, C> kademliaNode, I message) {
        if (message.isAlive()) {
            this.dhtKademliaNode.getRoutingTable().forceUpdate(message.getNode());
        }
        switch (message.getType()) {
            case MessageType.DHT_STORE:
                if (!(message instanceof DHTStoreKademliaMessage))
                    throw new IllegalArgumentException("Cant handle message. Required: DHTStoreKademliaMessage");
                return (O) handleStoreRequest((DHTStoreKademliaMessage<ID, C, K, V>) message);
            case MessageType.DHT_STORE_RESULT:
                if (!(message instanceof DHTStoreResultKademliaMessage))
                    throw new IllegalArgumentException("Cant handle message. Required: DHTStoreResultKademliaMessage");
                return (O) handleStoreResult((DHTStoreResultKademliaMessage<ID, C, K>) message);
            default:
                throw new IllegalArgumentException("message param is not supported");
        }
    }
}
