package io.Adrestus.core.Util;

import io.Adrestus.core.CommitteeBlock;
import io.Adrestus.core.TransactionBlock;

public class BlockSizeCalculator extends AbstractBlockSizeCalculator {

    public BlockSizeCalculator(TransactionBlock transactionBlock) {
        super(transactionBlock);
    }

    public BlockSizeCalculator(CommitteeBlock committeeBlock) {
        super(committeeBlock);
    }

    public BlockSizeCalculator() {
        super();
    }


    public int TransactionBlockSizeCalculator() {
        int TransactionListSize = transactionBlock.getTransactionList().size() == 0 ? 1024 : transactionBlock.getTransactionList().size() * 1024;
        int StakingTransactionListSize = transactionBlock.getStakingTransactionList().size() == 0 ? 1024 : transactionBlock.getStakingTransactionList().size() * 1024;
        final int[] InboundSize = {0};
        transactionBlock.getInbound().getMap_receipts().values().stream().forEach(value -> {
            value.entrySet().forEach(receipt -> {
                int SumMerkleProofs = receipt.getValue().stream().filter(element -> element.getProofs() != null).mapToInt(proof -> proof.getProofs().getLength()).sum();
                int ReceiptListSize = receipt.getValue().size() == 0 ? 1024 : receipt.getValue().size() * 1024;
                int ReceiptBlockSize = 1024;
                InboundSize[0] = InboundSize[0] + (ReceiptListSize + ReceiptBlockSize + SumMerkleProofs);
            });
        });
        final int[] OutBoundSize = {0};
        transactionBlock.getOutbound().getMap_receipts().values().stream().forEach(value -> {
            value.entrySet().forEach(receipt -> {
                int SumMerkleProofs = receipt.getValue().stream().filter(element -> element.getProofs() != null).mapToInt(proof -> proof.getProofs().getLength()).sum();
                int ReceiptListSize = receipt.getValue().size() == 0 ? 1024 : receipt.getValue().size() * 1024;
                int ReceiptBlockSize = 1024;
                OutBoundSize[0] = OutBoundSize[0] + (ReceiptListSize + ReceiptBlockSize + SumMerkleProofs);
            });
        });
        int SignatureDataSize = 2048 * transactionBlock.getSignatureData().size();
        int totalsize = 1024 + (TransactionListSize + StakingTransactionListSize + InboundSize[0] + OutBoundSize[0]) + SignatureDataSize + super.AbstractTransactionBlockSizeCalculator();
        this.transactionBlock = null;
        return totalsize;
    }

    public int CommitteeBlockSizeCalculator() {
        int ProposerArray = this.committeeBlock.getCommitteeProposer().length == 0 ? 0 : this.committeeBlock.getCommitteeProposer().length * 4;
        int StakingMapSize = this.committeeBlock.getStakingMap().size() == 0 ? 1024 : this.committeeBlock.getStakingMap().size() * 2048;
        final int[] StructureMapSize = {0};
        this.committeeBlock.getStructureMap().values().stream().forEach(value -> {
            int HashmapEntrySize = value.entrySet().size() == 0 ? 1024 : value.entrySet().size() * 1024;
            StructureMapSize[0] = StructureMapSize[0] + HashmapEntrySize;
        });
        int totalsize = 1024 + (ProposerArray + StakingMapSize + StructureMapSize[0]) + super.AbstractCommitteeBlockSizeCalculator();
        this.committeeBlock = null;
        return totalsize;
    }
}
