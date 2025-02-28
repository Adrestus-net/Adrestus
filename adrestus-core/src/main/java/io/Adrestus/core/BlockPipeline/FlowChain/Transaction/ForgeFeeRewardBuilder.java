package io.Adrestus.core.BlockPipeline.FlowChain.Transaction;

import io.Adrestus.core.*;
import io.Adrestus.core.BlockPipeline.BlockRequest;
import io.Adrestus.core.BlockPipeline.BlockRequestHandler;
import io.Adrestus.core.BlockPipeline.BlockRequestType;
import io.Adrestus.crypto.bls.model.CachedBLSKeyPair;
import lombok.SneakyThrows;

import java.math.BigDecimal;
import java.util.NoSuchElementException;

public class ForgeFeeRewardBuilder implements BlockRequestHandler<TransactionBlock> {
    private final IBlockIndex blockIndex;

    public ForgeFeeRewardBuilder() {
        this.blockIndex = new BlockIndex();
    }

    @Override
    public boolean canHandleRequest(BlockRequest<TransactionBlock> req) {
        return req.getRequestType() == BlockRequestType.FORGE_FEE_REWARD_BUILDER;
    }

    @Override
    public int getPriority() {
        return 2;
    }

    @Override
    @SneakyThrows
    public void process(BlockRequest<TransactionBlock> blockRequest) {
        if (!blockRequest.getBlock().getTransactionList().isEmpty()) {
            BigDecimal sum = blockRequest.getBlock().getTransactionList().parallelStream().filter(val -> !val.getType().equals(TransactionType.UNCLAIMED_FEE_REWARD)).map(Transaction::getAmountWithTransactionFee).reduce(BigDecimal.ZERO, BigDecimal::add);
            try {
                blockRequest.getBlock().getTransactionList().addFirst(new UnclaimedFeeRewardTransaction(TransactionType.UNCLAIMED_FEE_REWARD, this.blockIndex.getAddressByPublicKey(CachedBLSKeyPair.getInstance().getPublicKey()), sum));
            } catch (NoSuchElementException e) {
                throw new IllegalArgumentException("Leader Reward fee not added");
            }
        }
    }

    @Override
    public void clear(BlockRequest<TransactionBlock> blockRequest) {
        blockRequest.clear();
    }

    @Override
    public String name() {
        return "ForgeFeeRewardBuilder";
    }
}
