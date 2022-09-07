package io.Adrestus.core.RingBuffer.handler.blocks;

import io.Adrestus.core.RingBuffer.event.AbstractBlockEvent;
import io.Adrestus.core.TransactionBlock;
import io.Adrestus.core.Trie.MerkleNode;
import io.Adrestus.core.Trie.MerkleTreeImp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class TransactionsMerkleeEventHandler implements BlockEventHandler<AbstractBlockEvent> {
    private static Logger LOG = LoggerFactory.getLogger(TransactionsMerkleeEventHandler.class);
    private final MerkleTreeImp tree;
    private final List<MerkleNode> list;

    public TransactionsMerkleeEventHandler() {
        this.tree = new MerkleTreeImp();
        this.list = new ArrayList<MerkleNode>();
    }

    @Override
    public void onEvent(AbstractBlockEvent blockEvent, long l, boolean b) throws Exception {
        try {
            TransactionBlock block = (TransactionBlock) blockEvent.getBlock();
            if (block.getTransactionList().isEmpty()) {
                LOG.info("Empty Transaction List");
                return;
            }


            block.getTransactionList().stream().forEach(x -> {
                list.add(new MerkleNode(x.getHash()));
            });

            tree.my_generate2(list);


            if (!tree.getRootHash().equals(block.getMerkleRoot())) {
                LOG.info("Transaction Merklee root is not valid");
                return;
            }


        } catch (NullPointerException ex) {
            LOG.info("Block is empty");
        }
    }
}
