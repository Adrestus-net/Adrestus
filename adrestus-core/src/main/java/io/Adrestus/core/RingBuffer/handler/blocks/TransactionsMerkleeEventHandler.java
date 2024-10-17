package io.Adrestus.core.RingBuffer.handler.blocks;

import io.Adrestus.Trie.MerkleNode;
import io.Adrestus.Trie.MerkleTreeOldImp;
import io.Adrestus.core.RingBuffer.event.AbstractBlockEvent;
import io.Adrestus.core.StatusType;
import io.Adrestus.core.TransactionBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class TransactionsMerkleeEventHandler implements BlockEventHandler<AbstractBlockEvent> {
    private static Logger LOG = LoggerFactory.getLogger(TransactionsMerkleeEventHandler.class);
    private final MerkleTreeOldImp tree;

    public TransactionsMerkleeEventHandler() {
        this.tree = new MerkleTreeOldImp();
    }

    @Override
    public void onEvent(AbstractBlockEvent blockEvent, long l, boolean b) throws Exception {
        try {
            TransactionBlock transactionBlock = (TransactionBlock) blockEvent.getBlock();
            List<MerkleNode> list = new ArrayList<MerkleNode>();
            if (transactionBlock.getTransactionList().isEmpty()) {
                LOG.info("Empty Transaction List");
                return;
            }


            transactionBlock.getTransactionList().stream().forEach(x -> {
                list.add(new MerkleNode(x.getHash()));
            });

            tree.my_generate2(list);


            if (!tree.getRootHash().equals(transactionBlock.getMerkleRoot())) {
                LOG.info("Transaction Merklee root is not valid");
                transactionBlock.setStatustype(StatusType.ABORT);
                return;
            }


        } catch (NullPointerException ex) {
            LOG.info("Block is empty");
        }
    }
}
