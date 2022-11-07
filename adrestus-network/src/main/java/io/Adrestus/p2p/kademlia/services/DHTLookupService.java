package io.Adrestus.p2p.kademlia.services;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import io.Adrestus.p2p.kademlia.connection.ConnectionInfo;
import io.Adrestus.p2p.kademlia.model.FindNodeAnswer;
import io.Adrestus.p2p.kademlia.model.LookupAnswer;
import io.Adrestus.p2p.kademlia.node.DHTKademliaNodeAPI;
import io.Adrestus.p2p.kademlia.node.KademliaNodeAPI;
import io.Adrestus.p2p.kademlia.node.Node;
import io.Adrestus.p2p.kademlia.node.external.ExternalNode;
import io.Adrestus.p2p.kademlia.protocol.MessageType;
import io.Adrestus.p2p.kademlia.protocol.handler.MessageHandler;
import io.Adrestus.p2p.kademlia.protocol.message.DHTLookupKademliaMessage;
import io.Adrestus.p2p.kademlia.protocol.message.DHTLookupResultKademliaMessage;
import io.Adrestus.p2p.kademlia.protocol.message.EmptyKademliaMessage;
import io.Adrestus.p2p.kademlia.protocol.message.KademliaMessage;
import io.Adrestus.p2p.kademlia.util.DateUtil;
import io.Adrestus.p2p.kademlia.util.NodeUtil;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;


public class DHTLookupService<ID extends Number, C extends ConnectionInfo, K extends Serializable, V extends Serializable> implements MessageHandler<ID, C> {
    private final Map<K, Future<LookupAnswer<ID, K, V>>> lookupFutureMap = new ConcurrentHashMap<>();
    private final Map<K, LookupAnswer<ID, K, V>> lookupAnswerMap = new ConcurrentHashMap<>();

    private final DHTKademliaNodeAPI<ID, C, K, V> dhtKademliaNode;
    private final ListeningExecutorService listeningExecutorService;
    private final ExecutorService cleanupExecutor;
    private final ExecutorService handlerExecutorService;

    public DHTLookupService(
            DHTKademliaNodeAPI<ID, C, K, V> dhtKademliaNode,
            ExecutorService executorService,
            ExecutorService cleanupExecutor
    ) {
        this.dhtKademliaNode = dhtKademliaNode;
        this.listeningExecutorService = (executorService instanceof ListeningExecutorService) ? (ListeningExecutorService) executorService : MoreExecutors.listeningDecorator(executorService);
        this.cleanupExecutor = cleanupExecutor;
        this.handlerExecutorService = executorService;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <I extends KademliaMessage<ID, C, ?>, O extends KademliaMessage<ID, C, ?>> O handle(KademliaNodeAPI<ID, C> kademliaNode, I message) {
        if (message.isAlive()) {
            this.dhtKademliaNode.getRoutingTable().forceUpdate(message.getNode());
        }
        switch (message.getType()) {
            case MessageType.DHT_LOOKUP:
                if (!(message instanceof DHTLookupKademliaMessage))
                    throw new IllegalArgumentException("Cant handle message. Required: DHTLookupKademliaMessage");
                return (O) handleLookupRequest((DHTLookupKademliaMessage<ID, C, K>) message);
            case MessageType.DHT_LOOKUP_RESULT:
                if (!(message instanceof DHTLookupResultKademliaMessage))
                    throw new IllegalArgumentException("Cant handle message. Required: DHTLookupResultKademliaMessage");
                return (O) handleLookupResult((DHTLookupResultKademliaMessage<ID, C, K, V>) message);
            default:
                throw new IllegalArgumentException("message param is not supported");
        }
    }

    public void cleanUp() {
        this.lookupAnswerMap.forEach((k, idkvLookupAnswer) -> idkvLookupAnswer.finishWatch());
        this.lookupFutureMap.clear();
        this.lookupAnswerMap.clear();
    }

    public Future<LookupAnswer<ID, K, V>> lookup(K key) {
        synchronized (this) {
            Future<LookupAnswer<ID, K, V>> f = null;
            if ((f = lookupFutureMap.get(key)) != null) {
                return f;
            }
        }

        ListenableFuture<LookupAnswer<ID, K, V>> futureAnswer = this.listeningExecutorService.submit(
                () -> {
                    LookupAnswer<ID, K, V> lookupAnswer = handleLookup(this.dhtKademliaNode, this.dhtKademliaNode, key, 0);
                    if (lookupAnswer.getResult().equals(LookupAnswer.Result.FOUND) || lookupAnswer.getResult().equals(LookupAnswer.Result.FAILED)) {
                        return lookupAnswer;
                    }
                    lookupAnswerMap.put(key, lookupAnswer);
                    lookupAnswer.watch();
                    return lookupAnswer;
                });
        this.lookupFutureMap.put(key, futureAnswer);

        futureAnswer.addListener(() -> {
            this.lookupFutureMap.remove(key);
            this.lookupAnswerMap.remove(key);
        }, this.cleanupExecutor);

        return futureAnswer;
    }

    protected LookupAnswer<ID, K, V> handleLookup(Node<ID, C> caller, Node<ID, C> requester, K key, int currentTry) {
        // Check if current node contains data
        if (this.dhtKademliaNode.getKademliaRepository().contains(key)) {
            V value = this.dhtKademliaNode.getKademliaRepository().get(key);
            return getNewLookupAnswer(key, LookupAnswer.Result.FOUND, this.dhtKademliaNode, value);
        }

        // If max tries has reached then return failed
        if (currentTry == this.dhtKademliaNode.getNodeSettings().getIdentifierSize()) {
            return getNewLookupAnswer(key, LookupAnswer.Result.FAILED, this.dhtKademliaNode, null);
        }

        //Otherwise, ask the closest node we know to key
        return getDataFromClosestNodes(caller, requester, key, currentTry);
    }

    protected LookupAnswer<ID, K, V> getDataFromClosestNodes(Node<ID, C> caller, Node<ID, C> requester, K key, int currentTry) {
        ID hash = this.dhtKademliaNode.getKeyHashGenerator().generateHash(key);
        FindNodeAnswer<ID, C> findNodeAnswer = this.dhtKademliaNode.getRoutingTable().findClosest(hash);
        Date date = DateUtil.getDateOfSecondsAgo(this.dhtKademliaNode.getNodeSettings().getMaximumLastSeenAgeToConsiderAlive());
        for (ExternalNode<ID, C> externalNode : findNodeAnswer.getNodes()) {
            //ignore self because we already checked if current node holds the data or not
            //Also ignore nodeToIgnore if its not null
            if (externalNode.getId().equals(this.dhtKademliaNode.getId()) || (caller != null && externalNode.getId().equals(caller.getId())))
                continue;

            //if node is alive, ask for data
            if (NodeUtil.recentlySeenOrAlive(this.dhtKademliaNode, externalNode, date)) {
                KademliaMessage<ID, C, Serializable> response = this.dhtKademliaNode.getMessageSender().sendMessage(
                        this.dhtKademliaNode,
                        externalNode,
                        new DHTLookupKademliaMessage<>(
                                new DHTLookupKademliaMessage.DHTLookup<>(requester, key, currentTry + 1)
                        )
                );
                if (response.isAlive()) {
                    return getNewLookupAnswer(key, LookupAnswer.Result.PASSED, this.dhtKademliaNode, null);
                }
            }
        }

        return getNewLookupAnswer(key, LookupAnswer.Result.FAILED, this.dhtKademliaNode, null);

    }

    protected EmptyKademliaMessage<ID, C> handleLookupResult(DHTLookupResultKademliaMessage<ID, C, K, V> message) {
        DHTLookupResultKademliaMessage.DHTLookupResult<K, V> data = message.getData();
        LookupAnswer<ID, K, V> answer = this.lookupAnswerMap.get(data.getKey());
        if (answer != null) {
            answer.setResult(data.getResult());
            answer.setKey(data.getKey());
            answer.setValue(data.getValue());
            answer.setNodeId(message.getNode().getId());
            answer.finishWatch();
        }
        return new EmptyKademliaMessage<>();
    }

    protected EmptyKademliaMessage<ID, C> handleLookupRequest(DHTLookupKademliaMessage<ID, C, K> message) {
        this.handlerExecutorService.submit(() -> {
            DHTLookupKademliaMessage.DHTLookup<ID, C, K> data = message.getData();
            LookupAnswer<ID, K, V> lookupAnswer = handleLookup(this.dhtKademliaNode, data.getRequester(), data.getKey(), data.getCurrentTry());
            if (lookupAnswer.getResult().equals(LookupAnswer.Result.FAILED) || lookupAnswer.getResult().equals(LookupAnswer.Result.FOUND)) {
                this.dhtKademliaNode.getMessageSender().sendAsyncMessage(this.dhtKademliaNode, data.getRequester(), new DHTLookupResultKademliaMessage<>(
                        new DHTLookupResultKademliaMessage.DHTLookupResult<>(
                                lookupAnswer.getResult(),
                                data.getKey(),
                                lookupAnswer.getValue()
                        )
                ));
            }
        });

        return new EmptyKademliaMessage<>();
    }

    protected LookupAnswer<ID, K, V> getNewLookupAnswer(K k, LookupAnswer.Result result, Node<ID, C> node, @Nullable V value) {
        LookupAnswer<ID, K, V> lookupAnswer = new LookupAnswer<>();
        lookupAnswer.setAlive(true);
        lookupAnswer.setNodeId(node.getId());
        lookupAnswer.setKey(k);
        lookupAnswer.setResult(result);
        lookupAnswer.setValue(value);
        return lookupAnswer;
    }
}
