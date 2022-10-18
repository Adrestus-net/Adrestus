package io.Adrestus.core;

import io.Adrestus.config.AdrestusConfiguration;
import io.Adrestus.core.Resourses.CachedLatestBlocks;
import io.Adrestus.core.Resourses.MemoryPool;
import io.Adrestus.core.RingBuffer.publisher.BlockEventPublisher;
import io.Adrestus.core.Trie.MerkleNode;
import io.Adrestus.core.Trie.MerkleTree;
import io.Adrestus.core.Trie.MerkleTreeImp;
import io.Adrestus.crypto.HashUtil;
import io.Adrestus.util.GetTime;
import io.Adrestus.util.SerializationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class RegularBlock implements BlockForge {
    private static Logger LOG = LoggerFactory.getLogger(RegularBlock.class);

    private final SerializationUtil<AbstractBlock> encode;

    public RegularBlock() {
        encode = new SerializationUtil<AbstractBlock>(AbstractBlock.class);
    }

    @Override
    public void forgeTransactionBlock(TransactionBlock transactionBlock) throws Exception {
        BlockEventPublisher publisher = new BlockEventPublisher(1024);


        publisher
                .withDuplicateHandler()
                .withGenerationHandler()
                .withHashHandler()
                .withHeaderEventHandler()
                .withHeightEventHandler()
                .withTimestampEventHandler()
                .withTransactionMerkleeEventHandler()
                .mergeEventsAndPassVerifySig();


        MerkleTree tree = new MerkleTreeImp();
        ArrayList<MerkleNode> merkleNodeArrayList = new ArrayList<>();
        transactionBlock.getHeaderData().setPreviousHash(CachedLatestBlocks.getInstance().getTransactionBlock().getHash());
        transactionBlock.getHeaderData().setVersion(AdrestusConfiguration.version);
        transactionBlock.getHeaderData().setTimestamp(GetTime.GetTimeStampInString());
        transactionBlock.setStatustype(StatusType.PENDING);
        transactionBlock.setHeight(CachedLatestBlocks.getInstance().getTransactionBlock().getHeight() + 1);
        transactionBlock.setGeneration(CachedLatestBlocks.getInstance().getCommitteeBlock().getGeneration());
        transactionBlock.setViewID(CachedLatestBlocks.getInstance().getTransactionBlock().getViewID() + 1);
        transactionBlock.setZone(0);

        try {
            transactionBlock.setTransactionList(MemoryPool.getInstance().getAll());
            transactionBlock.getTransactionList().stream().forEach(x -> {
                merkleNodeArrayList.add(new MerkleNode(x.getHash()));
            });
            tree.my_generate2(merkleNodeArrayList);
            transactionBlock.setMerkleRoot(tree.getRootHash());
            byte[] tohash = encode.encode(transactionBlock);
            transactionBlock.setHash(HashUtil.sha256_bytetoString(tohash));
            publisher.start();
            publisher.publish(transactionBlock);
            publisher.getJobSyncUntilRemainingCapacityZero();

        } finally {
            publisher.close();
        }
    }

    @Override
    public void forgeCommitteBlock(CommitteeBlock committeeBlock) {
        BlockEventPublisher publisher = new BlockEventPublisher(1024);
        publisher
                .withHashHandler()
                .withTimestampEventHandler()
                .withHeightEventHandler()
                .withGenerationHandler()
                .mergeEvents();
    }
}
