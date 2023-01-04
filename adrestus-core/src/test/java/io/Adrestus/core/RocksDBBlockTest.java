package io.Adrestus.core;

import io.Adrestus.crypto.bls.BLS381.ECP;
import io.Adrestus.crypto.bls.BLS381.ECP2;
import io.Adrestus.crypto.bls.mapper.ECP2mapper;
import io.Adrestus.crypto.bls.mapper.ECPmapper;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.elliptic.mapper.BigIntegerSerializer;
import io.Adrestus.crypto.elliptic.mapper.CustomSerializerTreeMap;
import io.Adrestus.util.SerializationUtil;
import io.distributedLedger.DatabaseFactory;
import io.distributedLedger.DatabaseInstance;
import io.distributedLedger.DatabaseType;
import io.distributedLedger.IDatabase;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RocksDBBlockTest {


    @Test
    public void add_get2() {
        IDatabase<String, AbstractBlock> database = new DatabaseFactory(String.class, AbstractBlock.class).getDatabase(DatabaseType.ROCKS_DB, DatabaseInstance.ZONE_1_TRANSACTION_BLOCK);
        String hash = "Hash";
        TransactionBlock prevblock = new TransactionBlock();
        CommitteeBlock committeeBlock = new CommitteeBlock();
        committeeBlock.setGeneration(1);
        committeeBlock.setViewID(1);
        prevblock.setHeight(1);
        prevblock.setHash(hash);
        database.save(hash, prevblock);
        TransactionBlock copy = (TransactionBlock) database.findByKey(hash).get();
        assertEquals(prevblock, copy);
        System.out.println(copy.toString());
        database.delete_db();
    }

    @Test
    public void add_get3() {
        IDatabase<String, AbstractBlock> database = new DatabaseFactory(String.class, AbstractBlock.class).getDatabase(DatabaseType.ROCKS_DB);
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
        list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        list.add(new SerializationUtil.Mapping(TreeMap.class, ctx -> new CustomSerializerTreeMap()));
        SerializationUtil<AbstractBlock> serenc = new SerializationUtil<AbstractBlock>(AbstractBlock.class, list);
        String hash = "Hash";
        CommitteeBlock committeeBlock = new CommitteeBlock();
        committeeBlock.getStakingMap().put(10.0, null);
        committeeBlock.getStakingMap().put(13.0, null);
        committeeBlock.getStructureMap().get(0).put(new BLSPublicKey(), "");
        committeeBlock.getStructureMap().put(0, new LinkedHashMap<BLSPublicKey, String>());
        committeeBlock.setGeneration(1);
        committeeBlock.setViewID(1);
        committeeBlock.setHeight(1);
        committeeBlock.setHash(hash);
        committeeBlock.setDifficulty(119);
        committeeBlock.setVRF("sadsada");
        committeeBlock.setVDF("sadaa");
        CommitteeBlock cp = (CommitteeBlock) serenc.decode(serenc.encode(committeeBlock));
        assertEquals(committeeBlock, cp);
        database.save(hash, committeeBlock);
        CommitteeBlock copy = (CommitteeBlock) database.findByKey(hash).get();
        assertEquals(committeeBlock, copy);
        System.out.println(copy.toString());
        database.delete_db();
    }


    @Test
    public void delete() {
        IDatabase<String, AbstractBlock> database = new DatabaseFactory(String.class, AbstractBlock.class).getDatabase(DatabaseType.ROCKS_DB);
        String hash = "Hash";
        TransactionBlock prevblock = new TransactionBlock();
        CommitteeBlock committeeBlock = new CommitteeBlock();
        committeeBlock.setGeneration(1);
        committeeBlock.setViewID(1);
        prevblock.setHeight(1);
        prevblock.setHash(hash);
        database.save(hash, prevblock);
        TransactionBlock copy = (TransactionBlock) database.findByKey(hash).get();
        assertEquals(prevblock, copy);
        database.deleteByKey(hash);
        Optional<AbstractBlock> empty = database.findByKey(hash);
        assertEquals(Optional.empty(), empty);
        database.delete_db();
    }


    @Test
    public void find_by_list_key() {
        IDatabase<String, AbstractBlock> database = new DatabaseFactory(String.class, AbstractBlock.class).getDatabase(DatabaseType.ROCKS_DB);
        TransactionBlock transactionBlock1 = new TransactionBlock();
        transactionBlock1.setHash("hash1");

        TransactionBlock transactionBlock2 = new TransactionBlock();
        transactionBlock2.setHash("hash2");

        database.save("hash1", transactionBlock1);
        database.save("hash2", transactionBlock2);
        ArrayList<String> list = new ArrayList<>();
        list.add("hash1");
        list.add("hash2");

        List<AbstractBlock> values = database.findByListKey(list);
        assertEquals(transactionBlock1, values.get(0));
        assertEquals(transactionBlock2, values.get(1));

        database.delete_db();
    }

    @Test
    public void add_get4() throws Exception {


       /* SerializableFunction<PatriciaTreeNode, Bytes>  valueSerializer = value -> (value != null) ? Bytes.wrap("".getBytes(StandardCharsets.UTF_8)) : null;
        IMerklePatriciaTrie<Bytes, PatriciaTreeNode> patriciaTreeImp = new MerklePatriciaTrie<Bytes, PatriciaTreeNode>(valueSerializer);
        patriciaTreeImp.put(Bytes.wrap("1".getBytes(StandardCharsets.UTF_8)),new PatriciaTreeNode(10,0,0));
        System.out.println(patriciaTreeImp.getRootHash());
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream outstream = new ObjectOutputStream(byteOut);
        outstream.writeObject(patriciaTreeImp);*/


     /*   CachedZoneIndex.getInstance().setZoneIndex(1);
        IDatabase<String, RepositoryData> database = new DatabaseFactory(String.class, RepositoryData.class).getDatabase(DatabaseType.ROCKS_DB, DatabaseInstance.ZONE_1_TRANSACTION_BLOCK);
        MemoryTreePool m= (MemoryTreePool)TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex());
        m.store("address",new PatriciaTreeNode(1,0,0));
        m.store("address2",new PatriciaTreeNode(2,0,0));
        m.store("address3",new PatriciaTreeNode(3,0,0));
        String hash = "Hash";
        System.out.println(m.getRootHash());
        TransactionBlock prevblock = new TransactionBlock();
        prevblock.setHeight(1);
        prevblock.setHash(hash);
        RepositoryData repositoryData=new RepositoryData(prevblock, m);
        database.save(hash, repositoryData);
        RepositoryData copy = (RepositoryData) database.findByKey(hash).get();
        System.out.println(copy.getTree().getRootHash());
        assertEquals(prevblock, copy.getBlock());
        assertEquals(m, copy.getTree());
        System.out.println(copy.toString());
        database.delete_db();*/
    }

}
