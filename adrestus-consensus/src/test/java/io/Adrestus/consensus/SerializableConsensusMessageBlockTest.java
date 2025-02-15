package io.Adrestus.consensus;

import com.google.common.reflect.TypeToken;
import io.Adrestus.core.AbstractBlock;
import io.Adrestus.core.TransactionBlock;
import io.Adrestus.core.Util.BlockSizeCalculator;
import io.Adrestus.core.comparators.SortSignatureMapByBlsPublicKey;
import io.Adrestus.crypto.bls.BLS381.ECP;
import io.Adrestus.crypto.bls.BLS381.ECP2;
import io.Adrestus.crypto.bls.BLSSignatureData;
import io.Adrestus.crypto.bls.mapper.ECP2mapper;
import io.Adrestus.crypto.bls.mapper.ECPmapper;
import io.Adrestus.crypto.bls.model.BLSPrivateKey;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.bls.model.BLSSignature;
import io.Adrestus.crypto.bls.model.Signature;
import io.Adrestus.crypto.elliptic.mapper.BigDecimalSerializer;
import io.Adrestus.crypto.elliptic.mapper.BigIntegerSerializer;
import io.Adrestus.crypto.elliptic.mapper.CustomSerializerTreeMap;
import io.Adrestus.util.SerializationFuryUtil;
import io.Adrestus.util.SerializationUtil;
import io.distributedLedger.DatabaseFactory;
import io.distributedLedger.DatabaseType;
import io.distributedLedger.IDatabase;
import io.distributedLedger.ZoneDatabaseFactory;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SerializableConsensusMessageBlockTest {
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

    @SneakyThrows
    @BeforeAll
    public static void setup() {
        sizeCalculator = new BlockSizeCalculator();
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
        list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
        list.add(new SerializationUtil.Mapping(BigDecimal.class, ctx -> new BigDecimalSerializer()));
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
        IDatabase<String, AbstractBlock> database = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, ZoneDatabaseFactory.getZoneInstance(1));

        TreeMap<BLSPublicKey, BLSSignatureData> signatureData = new TreeMap<BLSPublicKey, BLSSignatureData>(new SortSignatureMapByBlsPublicKey());
        BLSSignatureData blsSignatureData1 = new BLSSignatureData();
        blsSignatureData1.getSignature()[0] = BLSSignature.sign("toSign".getBytes(StandardCharsets.UTF_8), sk1);
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
        database.save(String.valueOf(block.getHeight()), block);
        TransactionBlock copy = (TransactionBlock) database.findByKey("1").get();
        assertEquals(block, copy);

        database.delete_db();
    }

    @Test
    public void SerializeConsensusMessage() throws CloneNotSupportedException {

        String message = "toSign";
        TreeMap<BLSPublicKey, BLSSignatureData> signatureData = new TreeMap<BLSPublicKey, BLSSignatureData>(new SortSignatureMapByBlsPublicKey());
        BLSSignatureData blsSignatureData1 = new BLSSignatureData();
        BLSSignatureData blsSignatureData2 = new BLSSignatureData();
        BLSSignatureData blsSignatureData3 = new BLSSignatureData();
        BLSSignatureData blsSignatureData4 = new BLSSignatureData();

        blsSignatureData1.getSignature()[0] = BLSSignature.sign(message.getBytes(StandardCharsets.UTF_8), sk3);
        blsSignatureData1.getMessageHash()[0] = message;
        blsSignatureData2.getSignature()[0] = BLSSignature.sign(message.getBytes(StandardCharsets.UTF_8), sk4);
        blsSignatureData2.getMessageHash()[0] = message;
        blsSignatureData3.getSignature()[0] = BLSSignature.sign(message.getBytes(StandardCharsets.UTF_8), sk1);
        blsSignatureData3.getMessageHash()[0] = message;
        blsSignatureData4.getSignature()[0] = BLSSignature.sign(message.getBytes(StandardCharsets.UTF_8), sk2);
        blsSignatureData4.getMessageHash()[0] = message;
        signatureData.put(vk3, blsSignatureData1);
        signatureData.put(vk4, blsSignatureData2);
        signatureData.put(vk1, blsSignatureData3);
        signatureData.put(vk2, blsSignatureData4);
        TransactionBlock block = new TransactionBlock();
        block.setHash("1");
        block.setHeight(1);
        block.setSignatureData(signatureData);
        ConsensusMessage<TransactionBlock> consensusMessage = new ConsensusMessage<>(block);
        ConsensusMessage<TransactionBlock> consensusMessage2 = new ConsensusMessage<>(block);
        BLSPrivateKey sk1a = new BLSPrivateKey(1);
        BLSPublicKey vk1a = new BLSPublicKey(sk1a);
        consensusMessage2.getChecksumData().setBlsPublicKey(vk1a);
        consensusMessage2.getChecksumData().setSignature(BLSSignature.sign("toSign".getBytes(StandardCharsets.UTF_8), sk1));
        consensusMessage.getChecksumData().setBlsPublicKey(vk1);
        consensusMessage.getChecksumData().setSignature(BLSSignature.sign("toSign".getBytes(StandardCharsets.UTF_8), sk1));
        consensusMessage.getSignatures().putAll(signatureData);
        consensusMessage2.getSignatures().putAll(signatureData);
        assertEquals(consensusMessage, consensusMessage2);


        byte[] data = SerializationFuryUtil.getInstance().getFury().serialize(consensusMessage);
        ConsensusMessage<TransactionBlock> cloned = (ConsensusMessage<TransactionBlock>) SerializationFuryUtil.getInstance().getFury().deserialize(data);
        assertEquals(consensusMessage, cloned);
        assertEquals(consensusMessage2, cloned);

        BLSSignatureData blsSignatureData6 = new BLSSignatureData();
        blsSignatureData6.getSignature()[0] = new Signature(cloned.getChecksumData().getSignature().getPoint());
        consensusMessage.getSignatures().put(vk1, blsSignatureData6);
        byte[] hash2 = SerializationFuryUtil.getInstance().getFury().serialize(consensusMessage);
        ConsensusMessage<TransactionBlock> cloned2 = (ConsensusMessage<TransactionBlock>) SerializationFuryUtil.getInstance().getFury().deserialize(hash2);
        assertEquals(consensusMessage, cloned2);
    }

    @Test
    public void testList() {
        List<String> expected = new ArrayList<String>();
        expected.add("that");
        expected.add("another");

        List<String> actual = new ArrayList<String>();
        actual.add("another");
        actual.add("that");
        actual.add("pet");


        List<String> expected1 = new ArrayList<String>();
        expected1.add("that");
        expected1.add("another");

        List<String> actual1 = new ArrayList<String>();
        actual1.add("another");
        actual1.add("thata");
        actual1.add("pet");


        assertEquals(true, actual.containsAll(expected));
        assertEquals(false, actual1.containsAll(expected1));
    }
}
