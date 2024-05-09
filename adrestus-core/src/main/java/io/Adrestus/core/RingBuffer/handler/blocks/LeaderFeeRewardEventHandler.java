package io.Adrestus.core.RingBuffer.handler.blocks;

import io.Adrestus.core.BlockIndex;
import io.Adrestus.core.Resourses.CachedLeaderIndex;
import io.Adrestus.core.Resourses.CachedZoneIndex;
import io.Adrestus.core.RingBuffer.event.AbstractBlockEvent;
import io.Adrestus.core.StatusType;
import io.Adrestus.core.TransactionBlock;
import io.Adrestus.core.UnclaimedFeeRewardTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LeaderFeeRewardEventHandler implements BlockEventHandler<AbstractBlockEvent> {
    private static Logger LOG = LoggerFactory.getLogger(LeaderFeeRewardEventHandler.class);

    private final BlockIndex blockIndex;

    public LeaderFeeRewardEventHandler() {
        this.blockIndex = new BlockIndex();
    }

    @Override
    public void onEvent(AbstractBlockEvent blockEvent, long l, boolean b) throws Exception {
        TransactionBlock transactionBlock = (TransactionBlock) blockEvent.getBlock();
        if (transactionBlock.getTransactionList().isEmpty())
            return;
        try {
            UnclaimedFeeRewardTransaction transaction = (UnclaimedFeeRewardTransaction) transactionBlock.getTransactionList().get(0);
            String address = this.blockIndex.getAddressByPublicKey(this.blockIndex.getPublicKeyByIndex(CachedZoneIndex.getInstance().getZoneIndex(), CachedLeaderIndex.getInstance().getTransactionPositionLeader()));
            if (!address.equals(transaction.getRecipientAddress())) {
                LOG.info("Fee Transaction Reward leader address is incorrect abort");
                transactionBlock.setStatustype(StatusType.ABORT);
                return;
            }
        } catch (ClassCastException e) {
            LOG.info("First Transaction is invalid not an UnclaimedFeeRewardTransaction abort");
            transactionBlock.setStatustype(StatusType.ABORT);
            return;
        }
    }
}
