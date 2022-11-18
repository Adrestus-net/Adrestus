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
import io.distributedLedger.DatabaseType;
import io.distributedLedger.IDatabase;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RocksDBBlockTest {

    private static  IDatabase<String, AbstractBlock> database;

    @BeforeAll
    public static void  before(){
        database = new DatabaseFactory(String.class, AbstractBlock.class).getDatabase(DatabaseType.ROCKS_DB);
    }
    @Test
    public void add_get2() {
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
    }

    @Test
    public void add_get3() {
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
        list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx->new BigIntegerSerializer()));
        list.add(new SerializationUtil.Mapping(TreeMap.class, ctx->new CustomSerializerTreeMap()));
        SerializationUtil<AbstractBlock> serenc = new SerializationUtil<AbstractBlock>(AbstractBlock.class,list);
        String hash = "Hash";
        CommitteeBlock committeeBlock = new CommitteeBlock();
        committeeBlock.getStakingMap().put(10.0, null);
        committeeBlock.getStakingMap().put(13.0,null);
        committeeBlock.getStructureMap().get(0).put(new BLSPublicKey(),"");
        committeeBlock.getStructureMap().put(0, new LinkedHashMap<BLSPublicKey, String>());
        committeeBlock.setGeneration(1);
        committeeBlock.setViewID(1);
        committeeBlock.setHeight(1);
        committeeBlock.setHash(hash);
        committeeBlock.setDifficulty(119);
        committeeBlock.setVRF("sadsada");
        committeeBlock.setVDF("sadaa");
        CommitteeBlock cp= (CommitteeBlock) serenc.decode(serenc.encode(committeeBlock));
        assertEquals(committeeBlock,cp);
        database.save(hash, committeeBlock);
        CommitteeBlock copy = (CommitteeBlock) database.findByKey(hash).get();
        assertEquals( committeeBlock, copy);
        System.out.println(copy.toString());
    }


    @Test
    public void delete() {
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
    }

    @AfterAll
    public static void after(){
        database.delete_db();
    }
}