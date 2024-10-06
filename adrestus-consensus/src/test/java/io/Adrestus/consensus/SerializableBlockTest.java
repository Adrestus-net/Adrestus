package io.Adrestus.consensus;

import com.google.common.reflect.TypeToken;
import io.Adrestus.core.*;
import io.Adrestus.core.Util.BlockSizeCalculator;
import io.Adrestus.crypto.bls.BLS381.ECP;
import io.Adrestus.crypto.bls.BLS381.ECP2;
import io.Adrestus.crypto.bls.BLSSignatureData;
import io.Adrestus.crypto.bls.mapper.ECP2mapper;
import io.Adrestus.crypto.bls.mapper.ECPmapper;
import io.Adrestus.crypto.bls.model.BLSPrivateKey;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.bls.model.BLSSignature;
import io.Adrestus.crypto.bls.model.Signature;
import io.Adrestus.crypto.elliptic.ECDSASign;
import io.Adrestus.crypto.elliptic.ECDSASignatureData;
import io.Adrestus.crypto.elliptic.ECKeyPair;
import io.Adrestus.crypto.elliptic.mapper.BigIntegerSerializer;
import io.Adrestus.crypto.elliptic.mapper.CustomSerializerTreeMap;
import io.Adrestus.p2p.kademlia.repository.KademliaData;
import io.Adrestus.util.SerializationUtil;
import io.distributedLedger.DatabaseFactory;
import io.distributedLedger.DatabaseType;
import io.distributedLedger.IDatabase;
import io.distributedLedger.ZoneDatabaseFactory;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SerializableBlockTest {
    private static final Type fluentType = new TypeToken<ConsensusMessage<TransactionBlock>>() {
    }.getType();

    private static SerializationUtil<AbstractBlock> serenc;
    private static BLSPrivateKey sk1;
    private static BLSPublicKey vk1;

    private static BLSPrivateKey sk2;
    private static BLSPublicKey vk2;

    private static BLSPrivateKey sk3;
    private static BLSPublicKey vk3;

    private static BLSPrivateKey sk4;
    private static BLSPublicKey vk4;


    private static BLSPrivateKey sk5;
    private static BLSPublicKey vk5;

    private static BLSPrivateKey sk6;
    private static BLSPublicKey vk6;

    private static BLSPrivateKey sk7;
    private static BLSPublicKey vk7;

    private static BLSPrivateKey sk8;
    private static BLSPublicKey vk8;

    private static BLSPrivateKey sk9;
    private static BLSPublicKey vk9;

    private static BlockSizeCalculator sizeCalculator;
    private static SerializationUtil<ConsensusMessage> consensus_serialize;

    @SneakyThrows
    @BeforeAll
    public static void setup() {
        sizeCalculator = new BlockSizeCalculator();
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
        list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        list.add(new SerializationUtil.Mapping(TreeMap.class, ctx -> new CustomSerializerTreeMap()));
        serenc = new SerializationUtil<AbstractBlock>(AbstractBlock.class, list);

        sk1 = new BLSPrivateKey(1);
        vk1 = new BLSPublicKey(sk1);

        sk2 = new BLSPrivateKey(2);
        vk2 = new BLSPublicKey(sk2);

        sk3 = new BLSPrivateKey(3);
        vk3 = new BLSPublicKey(sk3);

        sk4 = new BLSPrivateKey(4);
        vk4 = new BLSPublicKey(sk4);


        sk5 = new BLSPrivateKey(5);
        vk5 = new BLSPublicKey(sk5);

        sk6 = new BLSPrivateKey(6);
        vk6 = new BLSPublicKey(sk6);

        sk7 = new BLSPrivateKey(7);
        vk7 = new BLSPublicKey(sk7);

        sk8 = new BLSPrivateKey(8);
        vk8 = new BLSPublicKey(sk8);
    }
    @Test
    public void SerializeBlockDatabase() {
        IDatabase<String, TransactionBlock> database = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, ZoneDatabaseFactory.getZoneInstance(1));

        TreeMap<BLSPublicKey, BLSSignatureData> signatureData = new TreeMap<BLSPublicKey, BLSSignatureData>(new SortSignatureMapByBlsPublicKey());
        BLSSignatureData blsSignatureData1 = new BLSSignatureData();
        blsSignatureData1.getSignature()[0]= BLSSignature.sign("toSign".getBytes(StandardCharsets.UTF_8), sk1);
        BLSSignatureData blsSignatureData2 = new BLSSignatureData();
        BLSSignatureData blsSignatureData3 = new BLSSignatureData();
        BLSSignatureData blsSignatureData4 = new BLSSignatureData();
        signatureData.put(vk3, blsSignatureData1);
        signatureData.put(vk4, blsSignatureData2);
        signatureData.put(vk1, blsSignatureData3);
        signatureData.put(vk2, blsSignatureData4);
        AbstractBlock block = new TransactionBlock();
        block.setHash("1");
        block.setHeight(1);
        block.setSignatureData(signatureData);
        database.save(String.valueOf(block.getHeight()),block);
        TransactionBlock copy = (TransactionBlock) database.findByKey("1").get();
        assertEquals(block, copy);
        System.out.println(copy.toString());
        database.delete_db();
    }

    @Test
    public void SerializeConsensusMessage() throws CloneNotSupportedException {
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
        list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        list.add(new SerializationUtil.Mapping(TreeMap.class, ctx -> new CustomSerializerTreeMap()));
        this.consensus_serialize = new SerializationUtil<ConsensusMessage>(fluentType, list);

        TreeMap<BLSPublicKey, BLSSignatureData> signatureData = new TreeMap<BLSPublicKey, BLSSignatureData>(new SortSignatureMapByBlsPublicKey());
        BLSSignatureData blsSignatureData1 = new BLSSignatureData();
        blsSignatureData1.getSignature()[0]= BLSSignature.sign("toSign".getBytes(StandardCharsets.UTF_8), sk1);
        BLSSignatureData blsSignatureData2 = new BLSSignatureData();
        BLSSignatureData blsSignatureData3 = new BLSSignatureData();
        BLSSignatureData blsSignatureData4 = new BLSSignatureData();
        signatureData.put(vk3, blsSignatureData1);
        signatureData.put(vk4, blsSignatureData2);
        signatureData.put(vk1, blsSignatureData3);
        signatureData.put(vk2, blsSignatureData4);
        TransactionBlock block = new TransactionBlock();
        block.setHash("1");
        block.setHeight(1);
        block.setSignatureData(signatureData);
        ConsensusMessage<TransactionBlock> consensusMessage = new ConsensusMessage<>(block);
        consensusMessage.getChecksumData().setBlsPublicKey(vk1);
        consensusMessage.getChecksumData().setSignature(BLSSignature.sign("toSign".getBytes(StandardCharsets.UTF_8), sk1));
        byte[] hash=consensus_serialize.encode(consensusMessage);
        ConsensusMessage<TransactionBlock> replica=this.consensus_serialize.decode(hash);
        assertEquals(consensusMessage, replica);
        BLSSignatureData blsSignatureData6 = new BLSSignatureData();
        blsSignatureData6.getSignature()[0]= new Signature(replica.getChecksumData().getSignature().getPoint());
        consensusMessage.getSignatures().put(vk1, blsSignatureData6);
        byte[] hash2=this.consensus_serialize.encode(consensusMessage);
        ConsensusMessage<TransactionBlock> clone=this.consensus_serialize.decode(hash2);
        assertEquals(consensusMessage, clone);
    }
}
