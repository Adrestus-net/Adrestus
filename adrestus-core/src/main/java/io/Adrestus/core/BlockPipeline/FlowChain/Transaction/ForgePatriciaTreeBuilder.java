package io.Adrestus.core.BlockPipeline.FlowChain.Transaction;

import io.Adrestus.MemoryTreePool;
import io.Adrestus.TreeFactory;
import io.Adrestus.Trie.PatriciaTreeTransactionType;
import io.Adrestus.config.RewardConfiguration;
import io.Adrestus.core.BlockPipeline.BlockRequest;
import io.Adrestus.core.BlockPipeline.BlockRequestHandler;
import io.Adrestus.core.BlockPipeline.BlockRequestType;
import io.Adrestus.core.Resourses.CachedInboundTransactionBlocks;
import io.Adrestus.core.Resourses.CachedZoneIndex;
import io.Adrestus.core.RewardMechanism.Request;
import io.Adrestus.core.RewardMechanism.RequestType;
import io.Adrestus.core.RewardMechanism.RewardChainBuilder;
import io.Adrestus.core.Transaction;
import io.Adrestus.core.TransactionBlock;
import io.Adrestus.core.TreePoolConstructBlock;
import lombok.SneakyThrows;

public class ForgePatriciaTreeBuilder implements BlockRequestHandler<TransactionBlock> {
    @Override
    public boolean canHandleRequest(BlockRequest<TransactionBlock> req) {
        return req.getRequestType() == BlockRequestType.FORGE_PATRICIA_TREE_BUILDER;
    }

    @Override
    public int getPriority() {
        return 5;
    }

    @Override
    @SneakyThrows
    public void process(BlockRequest<TransactionBlock> blockRequest) {
        MemoryTreePool replica = new MemoryTreePool(((MemoryTreePool) TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex())));
        if (!blockRequest.getBlock().getTransactionList().isEmpty()) {
            TreePoolConstructBlock.getInstance().visitForgeTreePool(blockRequest.getBlock(), replica);
        }
        if (!blockRequest.getBlock().getInbound().getMap_receipts().isEmpty())
            blockRequest.getBlock()
                    .getInbound()
                    .getMap_receipts()
                    .get(blockRequest.getBlock().getInbound().getMap_receipts().keySet().toArray()[0])
                    .entrySet()
                    .stream()
                    .forEach(entry -> {
                        entry.getValue().stream().forEach(receipt -> {
                            TransactionBlock block = CachedInboundTransactionBlocks.getInstance().retrieve(receipt.getZoneFrom(), receipt.getReceiptBlock().getHeight());
                            Transaction trx = block.getTransactionList().get(receipt.getPosition());
                            replica.deposit(PatriciaTreeTransactionType.REGULAR, trx.getFrom(), trx.getAmount(), trx.getAmountWithTransactionFee());
                        });

                    });
        if (CachedZoneIndex.getInstance().getZoneIndex() == 0 && blockRequest.getBlock().getHeight() % RewardConfiguration.BLOCK_REWARD_HEIGHT == 0) {
            RewardChainBuilder rewardChainBuilder = new RewardChainBuilder();
            rewardChainBuilder.makeRequest(new Request(RequestType.EFFECTIVE_STAKE, "EFFECTIVE_STAKE"));
            rewardChainBuilder.makeRequest(new Request(RequestType.EFFECTIVE_STAKE_RATIO, "EFFECTIVE_STAKE_RATIO"));
            rewardChainBuilder.makeRequest(new Request(RequestType.DELEGATE_WEIGHTS_CALCULATOR, "DELEGATE_WEIGHTS_CALCULATOR"));
            rewardChainBuilder.makeRequest(new Request(RequestType.VALIDATOR_REWARD_CALCULATOR, "VALIDATOR_REWARD_CALCULATOR"));
            rewardChainBuilder.makeRequest(new Request(RequestType.DELEGATE_REWARD_CALCULATOR, "DELEGATE_REWARD_CALCULATOR"));
            rewardChainBuilder.makeRequest(new Request(RequestType.REWARD_STORAGE_CALCULATOR, "REWARD_STORAGE_CALCULATOR", replica));
        }

        blockRequest.getBlock().setPatriciaMerkleRoot(replica.getRootHash());
    }

    @Override
    public void clear(BlockRequest<TransactionBlock> blockRequest) {
        blockRequest.clear();
    }

    @Override
    public String name() {
        return "ForgePatriciaTreeBuilder";
    }
}
