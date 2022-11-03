package io.Adrestus.p2p.kademlia.serialization;

import com.google.gson.InstanceCreator;
import io.Adrestus.p2p.kademlia.common.NettyConnectionInfo;
import io.Adrestus.p2p.kademlia.common.NettyExternalNode;
import io.Adrestus.p2p.kademlia.node.Node;

import java.lang.reflect.Type;
import java.math.BigInteger;


public class NodeInstanceCreator implements InstanceCreator<Node<BigInteger, NettyConnectionInfo>> {
    @Override
    public Node<BigInteger, NettyConnectionInfo> createInstance(Type type) {
        return new NettyExternalNode();
    }
}
