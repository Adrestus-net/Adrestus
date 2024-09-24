package io.Adrestus.core.RingBuffer.handler.blocks;

import io.Adrestus.MemoryTreePool;
import io.Adrestus.TreeFactory;
import io.Adrestus.Trie.PatriciaTreeNode;
import io.Adrestus.Trie.PatriciaTreeTransactionType;
import io.Adrestus.config.RewardConfiguration;
import io.Adrestus.core.Resourses.CachedInboundTransactionBlocks;
import io.Adrestus.core.Resourses.CachedZoneIndex;
import io.Adrestus.core.RewardMechanism.*;
import io.Adrestus.core.RingBuffer.event.AbstractBlockEvent;
import io.Adrestus.core.StatusType;
import io.Adrestus.core.Transaction;
import io.Adrestus.core.TransactionBlock;
import io.Adrestus.core.TreePoolConstructBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class PatriciaTreeEventHandler implements BlockEventHandler<AbstractBlockEvent> {
    private static Logger LOG = LoggerFactory.getLogger(PatriciaTreeEventHandler.class);

    @Override
    public void onEvent(AbstractBlockEvent blockEvent, long l, boolean b) throws Exception {
        TransactionBlock transactionBlock = (TransactionBlock) blockEvent.getBlock();
        MemoryTreePool replica = (MemoryTreePool) TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).clone();

        if (!transactionBlock.getTransactionList().isEmpty()) {
            TreePoolConstructBlock.getInstance().visitForgeTreePool(transactionBlock, replica);
        }

        CachedInboundTransactionBlocks.getInstance().generate(transactionBlock.getInbound().getMap_receipts(), transactionBlock.getGeneration());
        if (!transactionBlock.getInbound().getMap_receipts().isEmpty()) {
            transactionBlock
                    .getInbound()
                    .getMap_receipts()
                    .get(transactionBlock.getInbound().getMap_receipts().keySet().toArray()[0])
                    .entrySet()
                    .stream()
                    .forEach(entry -> {
                        entry.getValue().stream().forEach(receipt -> {
                            TransactionBlock block = CachedInboundTransactionBlocks.getInstance().retrieve(receipt.getZoneFrom(), receipt.getReceiptBlock().getHeight());
                            Transaction trx = block.getTransactionList().get(receipt.getPosition());
                            replica.deposit(PatriciaTreeTransactionType.REGULAR, trx.getFrom(), trx.getAmount(), trx.getAmountWithTransactionFee());
                        });
                    });
        }

        if(CachedZoneIndex.getInstance().getZoneIndex()==0 && transactionBlock.getHeight()% RewardConfiguration.BLOCK_REWARD_HEIGHT ==0) {
            RewardChainBuilder rewardChainBuilder = new RewardChainBuilder();
            rewardChainBuilder.makeRequest(new Request(RequestType.EFFECTIVE_STAKE, "EFFECTIVE_STAKE"));
            rewardChainBuilder.makeRequest(new Request(RequestType.EFFECTIVE_STAKE_RATIO, "EFFECTIVE_STAKE_RATIO"));
            rewardChainBuilder.makeRequest(new Request(RequestType.DELEGATE_WEIGHTS_CALCULATOR, "DELEGATE_WEIGHTS_CALCULATOR"));
            rewardChainBuilder.makeRequest(new Request(RequestType.VALIDATOR_REWARD_CALCULATOR, "VALIDATOR_REWARD_CALCULATOR"));
            rewardChainBuilder.makeRequest(new Request(RequestType.DELEGATE_REWARD_CALCULATOR, "DELEGATE_REWARD_CALCULATOR"));
            rewardChainBuilder.makeRequest(new Request(RequestType.REWARD_STORAGE_CALCULATOR, "REWARD_STORAGE_CALCULATOR", replica));
        }

        if (!replica.getRootHash().equals(transactionBlock.getPatriciaMerkleRoot())) {
            LOG.info("Patricia Merkle root is invalid abort");
            transactionBlock.setStatustype(StatusType.ABORT);
            return;
        }
    }
}
