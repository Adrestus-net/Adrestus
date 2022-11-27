package io.Adrestus.p2p.kademlia.protocol.message;

import com.google.common.base.Objects;
import io.Adrestus.p2p.kademlia.connection.ConnectionInfo;
import io.Adrestus.p2p.kademlia.node.Node;
import io.Adrestus.p2p.kademlia.protocol.MessageType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

@ToString
public class DHTLookupKademliaMessage<ID extends Number, C extends ConnectionInfo, K extends Serializable> extends KademliaMessage<ID, C, DHTLookupKademliaMessage.DHTLookup<ID, C, K>> {

    public DHTLookupKademliaMessage(DHTLookup<ID, C, K> data) {
        this();
        setData(data);
    }

    public DHTLookupKademliaMessage() {
        super(MessageType.DHT_LOOKUP);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static class DHTLookup<ID extends Number, C extends ConnectionInfo, K extends Serializable> implements Serializable {
        private Node<ID, C> requester;
        private K key;
        private int currentTry;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DHTLookup<?, ?, ?> dhtLookup = (DHTLookup<?, ?, ?>) o;
            return getCurrentTry() == dhtLookup.getCurrentTry() && Objects.equal(getRequester(), dhtLookup.getRequester()) && Objects.equal(getKey(), dhtLookup.getKey());
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(getRequester(), getKey(), getCurrentTry());
        }
    }

}
