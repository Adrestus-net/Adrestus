package io.Adrestus.core;

import com.google.common.primitives.Ints;
import io.Adrestus.config.AdrestusConfiguration;
import io.Adrestus.core.Resourses.CachedLatestBlocks;
import io.Adrestus.core.Resourses.CachedSecurityHeaders;
import io.Adrestus.core.Resourses.MemoryPool;
import io.Adrestus.core.RingBuffer.publisher.BlockEventPublisher;
import io.Adrestus.core.Trie.MerkleNode;
import io.Adrestus.core.Trie.MerkleTree;
import io.Adrestus.core.Trie.MerkleTreeImp;
import io.Adrestus.crypto.HashUtil;
import io.Adrestus.crypto.bls.BLS381.ECP;
import io.Adrestus.crypto.bls.BLS381.ECP2;
import io.Adrestus.crypto.bls.mapper.ECP2mapper;
import io.Adrestus.crypto.bls.mapper.ECPmapper;
import io.Adrestus.crypto.elliptic.mapper.BigIntegerSerializer;
import io.Adrestus.crypto.vdf.engine.VdfEngine;
import io.Adrestus.crypto.vdf.engine.VdfEnginePietrzak;
import io.Adrestus.util.CustomSerializerMap;
import io.Adrestus.util.GetTime;
import io.Adrestus.util.SerializationUtil;
import io.distributedLedger.DatabaseFactory;
import io.distributedLedger.DatabaseType;
import io.distributedLedger.IDatabase;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class RegularBlock implements BlockForge {
    private static Logger LOG = LoggerFactory.getLogger(RegularBlock.class);

    private final SerializationUtil<AbstractBlock> encode;

    public RegularBlock() {
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
        list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx->new BigIntegerSerializer()));
        list.add(new SerializationUtil.Mapping(TreeMap.class, ctx->new CustomSerializerMap()));
        encode = new SerializationUtil<AbstractBlock>(AbstractBlock.class,list);
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

    @SneakyThrows
    @Override
    public void forgeCommitteBlock(CommitteeBlock committeeBlock) {
        VdfEngine vdf = new VdfEnginePietrzak(2048);
        IDatabase<String, CommitteeBlock> database = new DatabaseFactory(String.class, CommitteeBlock.class).getDatabase(DatabaseType.ROCKS_DB);

        committeeBlock.setCommitteeProposer(new int[committeeBlock.getStakingMap().size()]);
        committeeBlock.setGeneration(CachedLatestBlocks.getInstance().getCommitteeBlock().getGeneration()+1);
        committeeBlock.getHeaderData().setPreviousHash(CachedLatestBlocks.getInstance().getCommitteeBlock().getHash());
        committeeBlock.setHeight(CachedLatestBlocks.getInstance().getCommitteeBlock().getHeight()+1);
        committeeBlock.getHeaderData().setTimestamp(GetTime.GetTimeStampInString());
        committeeBlock.setVRF(Hex.toHexString(CachedSecurityHeaders.getInstance().getSecurityHeader().getpRnd()));


       // ###################find difficulty##########################
        int finish = database.findDBsize();

        int n = finish;
        int summdiffuclty = 0;
        long sumtime = 0;
        Map<String, CommitteeBlock> block_entries = database.seekBetweenRange(0, finish);
        ArrayList<String> entries = new ArrayList<String>(block_entries.keySet());

        if (entries.size() == 1) {
            summdiffuclty = block_entries.get(entries.get(0)).getDifficulty();
            sumtime = 100;
        }

        for (int i = 0; i < entries.size(); i++) {
            if (i == entries.size() - 1)
                break;

            long older = GetTime.GetTimestampFromString(block_entries.get(entries.get(i)).getHeaderData().getTimestamp()).getTime();
            long newer = GetTime.GetTimestampFromString(block_entries.get(entries.get(i + 1)).getHeaderData().getTimestamp()).getTime();
            sumtime = sumtime + (newer - older);
            //System.out.println("edw "+(newer - older));
            summdiffuclty = summdiffuclty + block_entries.get(entries.get(i)).getDifficulty();
            //  System.out.println("edw "+(newer - older));
            if ((newer - older) > 1000) {
                int h = i;
            }
        }

        double d = ((double) summdiffuclty / n);
        // String s=String.format("%4d",  sumtime / n);
        double t = ((double) sumtime / n);
        //  System.out.println(t);
        int difficulty = (int) Math.round(t * ((double) AdrestusConfiguration.INIT_VDF_DIFFICULTY / d));
        committeeBlock.setDifficulty(difficulty * 2);
        committeeBlock.setVDF(Hex.toHexString(vdf.solve(Hex.decode(committeeBlock.getVRF()), committeeBlock.getDifficulty())));
        // ###################find VDF difficulty##########################

        // ###################Random assign validators##########################
        SecureRandom secureRandom = SecureRandom.getInstance(AdrestusConfiguration.ALGORITHM, AdrestusConfiguration.PROVIDER);
        secureRandom.setSeed(Hex.decode(committeeBlock.getVDF()));
        for (Map.Entry<Double, ValidatorAddressData> entry : committeeBlock.getStakingMap().entrySet()) {
            int nextInt = secureRandom.nextInt(AdrestusConfiguration.MAX_ZONES);
            committeeBlock
                    .getStructureMap()
                    .get(nextInt)
                    .put(entry.getValue().getValidatorBlSPublicKey(), entry.getValue().getIp());
        }
        int iteration=0;
        ArrayList<Integer>replica=new ArrayList<>();
        while(iteration<committeeBlock.getStakingMap().size()) {
            int nextInt = secureRandom.nextInt(committeeBlock.getStakingMap().size());
            if(!replica.contains(nextInt)) {
                replica.add(nextInt);
                iteration++;
            }
        }
        //###################Random assign validators##########################

        committeeBlock.setCommitteeProposer(Ints.toArray(replica));
        //########################################################################
        String hash = HashUtil.sha256_bytetoString(encode.encode(committeeBlock));
        committeeBlock.setHash(hash);
    }
}
