package io.Adrestus.core.RingBuffer.handler.receipts;

import io.Adrestus.Trie.MerkleNode;
import io.Adrestus.Trie.MerkleTreeOldImp;
import io.Adrestus.core.ReceiptBlock;
import io.Adrestus.core.RingBuffer.event.ReceiptBlockEvent;
import io.Adrestus.core.StatusType;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class OutboundMerkleEventHandler implements ReceiptEventHandler<ReceiptBlockEvent> {

    private static Logger LOG = LoggerFactory.getLogger(OutboundMerkleEventHandler.class);

    @Override
    public void onEvent(ReceiptBlockEvent receiptBlockEvent, long l, boolean b) throws InterruptedException {
        ReceiptBlock receiptBlock = receiptBlockEvent.getReceiptBlock();
        final MerkleTreeOldImp outer_tree = new MerkleTreeOldImp();
        final ArrayList<MerkleNode> merkleNodeArrayList = new ArrayList<>();
        receiptBlock.getTransactionBlock().getTransactionList().forEach(val -> merkleNodeArrayList.add(new MerkleNode(val.getHash())));
        outer_tree.my_generate2(merkleNodeArrayList);

        boolean bool1 = StringUtils.equals(receiptBlock.getTransactionBlock().getMerkleRoot(), outer_tree.GenerateRoot(receiptBlock.getReceipt().getProofs()));
        boolean bool2 = StringUtils.equals(receiptBlock.getReceipt().getReceiptBlock().getOutboundMerkleRoot(), outer_tree.getRootHash());

        if (!bool1 || !bool2) {
            LOG.info("Merkle tree is not valid abort");
            receiptBlockEvent.getReceiptBlock().setStatusType(StatusType.ABORT);
            return;
        }
    }
}
