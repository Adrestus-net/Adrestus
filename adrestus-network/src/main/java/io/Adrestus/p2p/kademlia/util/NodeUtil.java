package io.Adrestus.p2p.kademlia.util;

import io.Adrestus.p2p.kademlia.connection.ConnectionInfo;
import io.Adrestus.p2p.kademlia.exception.HandlerNotFoundException;
import io.Adrestus.p2p.kademlia.node.KademliaNodeAPI;
import io.Adrestus.p2p.kademlia.node.external.ExternalNode;
import io.Adrestus.p2p.kademlia.protocol.message.KademliaMessage;
import io.Adrestus.p2p.kademlia.protocol.message.PingKademliaMessage;
import lombok.extern.slf4j.Slf4j;
import java.util.Date;

@Slf4j
public class NodeUtil {

    /**
     * Check if a node is seen recently or is alive
     * @param kademliaNodeAPI Node to send message from
     * @param externalNode Node to check
     * @param date Date to use for comparing
     * @return boolean True if node is alive
     */
    public static <ID extends Number, C extends ConnectionInfo> boolean recentlySeenOrAlive(KademliaNodeAPI<ID, C> kademliaNodeAPI, ExternalNode<ID, C> externalNode, Date date) {
        if (externalNode.getLastSeen().after(date))
            return true;
        KademliaMessage<ID, C, ?> pingAnswer = kademliaNodeAPI.getMessageSender().sendMessage(kademliaNodeAPI, externalNode, new PingKademliaMessage<>());
        if (!pingAnswer.isAlive()){
            try {
                kademliaNodeAPI.onMessage(pingAnswer);
            } catch (HandlerNotFoundException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return pingAnswer.isAlive();
    }
}
