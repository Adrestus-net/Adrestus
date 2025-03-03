package io.Adrestus.p2p.kademlia.protocol.handler;

import io.Adrestus.p2p.kademlia.connection.ConnectionInfo;
import io.Adrestus.p2p.kademlia.exception.FullBucketException;
import io.Adrestus.p2p.kademlia.exception.HandlerNotFoundException;
import io.Adrestus.p2p.kademlia.node.KademliaNodeAPI;
import io.Adrestus.p2p.kademlia.protocol.message.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class FindNodeResponseMessageHandler<ID extends Number, C extends ConnectionInfo> implements MessageHandler<ID, C> {
    private static final Logger logger = LoggerFactory.getLogger(FindNodeResponseMessageHandler.class);

    private final ExecutorService executorService = Executors.newFixedThreadPool(1);

    @Override
    @SuppressWarnings("unchecked")
    public <I extends KademliaMessage<ID, C, ?>, O extends KademliaMessage<ID, C, ?>> O handle(KademliaNodeAPI<ID, C> kademliaNode, I message) {
        executorService.submit(() -> ((FindNodeResponseMessage<ID, C>) message).getData().getNodes().forEach(externalNode -> {
            // ignore self
            if (externalNode.getId().equals(kademliaNode.getId())) {
                return;
            }
            try {
                KademliaMessage<ID, C, Serializable> response = kademliaNode.getMessageSender().sendMessage(kademliaNode, externalNode, new PingKademliaMessage<>());
                if (response.isAlive() && kademliaNode.getRoutingTable().update(response.getNode())) {
                    FindNodeRequestMessage<ID, C> findNodeRequestMessage = new FindNodeRequestMessage<>();
                    findNodeRequestMessage.setData(kademliaNode.getId());
                    KademliaMessage<ID, C, Serializable> findNodeResponse = kademliaNode.getMessageSender().sendMessage(kademliaNode, message.getNode(), findNodeRequestMessage);
                    kademliaNode.onMessage(findNodeResponse);
                }
            } catch (HandlerNotFoundException | FullBucketException e) {
                logger.error(e.getMessage(), e);
            }
        }));
        return (O) new EmptyKademliaMessage<ID, C>();
    }
}
