package io.Adrestus.p2p.kademlia.helpers;

import io.Adrestus.p2p.kademlia.connection.ConnectionInfo;
import io.Adrestus.p2p.kademlia.connection.MessageSender;
import io.Adrestus.p2p.kademlia.exception.HandlerNotFoundException;
import io.Adrestus.p2p.kademlia.node.KademliaNodeAPI;
import io.Adrestus.p2p.kademlia.node.Node;
import io.Adrestus.p2p.kademlia.node.decorators.DateAwareNodeDecorator;
import io.Adrestus.p2p.kademlia.protocol.message.EmptyKademliaMessage;
import io.Adrestus.p2p.kademlia.protocol.message.KademliaMessage;
import lombok.SneakyThrows;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestMessageSenderAPI<ID extends Number, C extends ConnectionInfo> implements MessageSender<ID, C> {
    public final Map<ID, KademliaNodeAPI<ID, C>> map = new HashMap<>();
    private final ExecutorService executorService = Executors.newFixedThreadPool(1);

    public void registerNode(KademliaNodeAPI<ID, C> node) {
        map.put(node.getId(), node);
    }

    @SneakyThrows
    @Override
    @SuppressWarnings("unchecked")
    public <I extends Serializable, O extends Serializable> KademliaMessage<ID, C, I> sendMessage(KademliaNodeAPI<ID, C> caller, Node<ID, C> receiver, KademliaMessage<ID, C, O> message) {
        if (!this.map.containsKey(receiver.getId())) {
            EmptyKademliaMessage<ID, C> kademliaMessage = new EmptyKademliaMessage<>();
            kademliaMessage.setAlive(false);
            kademliaMessage.setNode(receiver);
            return (KademliaMessage<ID, C, I>) kademliaMessage;
        }

        message.setNode(new DateAwareNodeDecorator<>(caller));
        var response = (KademliaMessage<ID, C, I>) this.map.get(receiver.getId()).onMessage(message);
        response.setNode(new DateAwareNodeDecorator<>(receiver));
        return response;
    }

    @SneakyThrows
    @Override
    public <O extends Serializable> void sendAsyncMessage(KademliaNodeAPI<ID, C> caller, Node<ID, C> receiver, KademliaMessage<ID, C, O> message) {
        message.setNode(caller);
        this.executorService.submit(() -> {
            try {
                map.get(receiver.getId()).onMessage(message);
            } catch (HandlerNotFoundException ignored) {
            }
        });
    }

    public void stopAll() {
        for (KademliaNodeAPI<ID, C> kademliaNodeAPI : this.map.values()) {
            kademliaNodeAPI.stopNow();
        }
    }
}
