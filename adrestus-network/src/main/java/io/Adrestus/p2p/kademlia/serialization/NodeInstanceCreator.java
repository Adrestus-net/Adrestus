package io.Adrestus.p2p.kademlia.serialization;

import com.google.gson.InstanceCreator;
import io.Adrestus.p2p.kademlia.common.NettyConnectionInfo;
import io.Adrestus.p2p.kademlia.common.NettyExternalNode;
import io.Adrestus.p2p.kademlia.node.Node;

import java.lang.reflect.Type;

public class NodeInstanceCreator implements InstanceCreator<Node<Long, NettyConnectionInfo>> {
    @Override
    public Node<Long, NettyConnectionInfo> createInstance(Type type) {
        return new NettyExternalNode();
    }
}
