package io.Adrestus.core;

import io.Adrestus.TreeFactory;
import io.Adrestus.Trie.PatriciaTreeNode;
import io.Adrestus.config.AdrestusConfiguration;
import io.Adrestus.core.Resourses.CachedZoneIndex;
import io.Adrestus.core.Util.BlockSizeCalculator;
import io.Adrestus.crypto.HashUtil;
import io.Adrestus.crypto.WalletAddress;
import io.Adrestus.crypto.bls.BLS381.ECP;
import io.Adrestus.crypto.bls.BLS381.ECP2;
import io.Adrestus.crypto.bls.mapper.ECP2mapper;
import io.Adrestus.crypto.bls.mapper.ECPmapper;
import io.Adrestus.crypto.bls.model.BLSPrivateKey;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.elliptic.ECDSASign;
import io.Adrestus.crypto.elliptic.ECDSASignatureData;
import io.Adrestus.crypto.elliptic.ECKeyPair;
import io.Adrestus.crypto.elliptic.Keys;
import io.Adrestus.crypto.elliptic.mapper.*;
import io.Adrestus.crypto.mnemonic.Mnemonic;
import io.Adrestus.crypto.mnemonic.MnemonicException;
import io.Adrestus.crypto.mnemonic.Security;
import io.Adrestus.crypto.mnemonic.WordList;
import io.Adrestus.util.GetTime;
import io.Adrestus.util.SerializationUtil;
import io.activej.bytebuf.ByteBuf;
import io.activej.bytebuf.ByteBufPool;
import io.activej.serializer.BinaryInput;
import io.activej.serializer.BinarySerializer;
import io.activej.serializer.SerializerFactory;
import io.activej.serializer.annotations.Serialize;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.fury.Fury;
import org.apache.fury.ThreadSafeFury;
import org.apache.fury.config.CompatibleMode;
import org.apache.fury.config.Language;
import org.apache.fury.logging.LoggerFactory;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.apache.tuweni.bytes.MutableBytes;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.spongycastle.util.encoders.Hex;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;


public class SerializationPerformanceTest {
    private static final int version = 0x00;
    private static final int transactionSize = 10000;
    private static final int fixedSize = 10;
    private static BLSPrivateKey sk1;
    private static BLSPublicKey vk1;
    private static ECDSASign ecdsaSign = new ECDSASign();
    private static ArrayList<String> addreses = new ArrayList<>();
    private static ArrayList<ECKeyPair> keypair = new ArrayList<>();
    private static ArrayList<Transaction> transactions = new ArrayList<>();
    private static TransactionBlock transactionBlock = new TransactionBlock();
    private static SerializationUtil<Transaction> trx_serence;
    private static SerializationUtil<AbstractBlock> transcion_block;
    private static ThreadSafeFury fury;
    private static DynamicSerializer<Transaction> dynamicSerializer;
    private static int length;

    static {
        LoggerFactory.disableLogging();
    }

    @BeforeAll
    public static void setup() throws NoSuchAlgorithmException, NoSuchProviderException, MnemonicException, InvalidAlgorithmParameterException {
        sk1 = new BLSPrivateKey(1);
        vk1 = new BLSPublicKey(sk1);
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
        list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
        list.add(new SerializationUtil.Mapping(BigDecimal.class, ctx -> new BigDecimalSerializer()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        list.add(new SerializationUtil.Mapping(TreeMap.class, ctx -> new CustomSerializerTreeMap()));
        list.add(new SerializationUtil.Mapping(Bytes.class, ctx -> new BytesSerializer()));
        list.add(new SerializationUtil.Mapping(Bytes32.class, ctx -> new Bytes32Serializer()));
        list.add(new SerializationUtil.Mapping(MutableBytes.class, ctx -> new MutableBytesSerializer()));
        List<SerializationUtil.Mapping> list2 = new ArrayList<>();
        list2.add(new SerializationUtil.Mapping(BigDecimal.class, ctx -> new BigDecimalSerializer()));
        list2.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        trx_serence = new SerializationUtil<Transaction>(Transaction.class, list2);
        transcion_block = new SerializationUtil<AbstractBlock>(AbstractBlock.class, list);
        dynamicSerializer = new DynamicSerializer<Transaction>(Transaction.class);
        fury = Fury.builder()
                .withLanguage(Language.JAVA)
                .withRefTracking(false)
                .withRefCopy(false)
                .withClassVersionCheck(true)
                .withCompatibleMode(CompatibleMode.SCHEMA_CONSISTENT)
                .withAsyncCompilation(true)
                .withCodegen(false)
                .requireClassRegistration(false)
                .buildThreadSafeFury();
        SecureRandom random;
        String mnemonic_code = "fd8cee9c1a3f3f57ab51b25740b24341ae093c8f697fde4df948050d3acd1700f6379d716104d2159e4912509c40ac81714d833e93b822e5ba0fadd68d5568a2";
        random = SecureRandom.getInstance(AdrestusConfiguration.ALGORITHM, AdrestusConfiguration.PROVIDER);
        random.setSeed(Hex.decode(mnemonic_code));


        List<SerializationUtil.Mapping> list3 = new ArrayList<>();
        list3.add(new SerializationUtil.Mapping(BigDecimal.class, ctx -> new BigDecimalSerializer()));
        list3.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        SerializationUtil<Transaction> serenc = new SerializationUtil<Transaction>(Transaction.class, list3);

        for (int i = 0; i < fixedSize; i++) {
            Mnemonic mnem = new Mnemonic(Security.NORMAL, WordList.ENGLISH);
            char[] mnemonic_sequence = mnem.create();
            char[] passphrase = "p4ssphr4se".toCharArray();
            byte[] key = mnem.createSeed(mnemonic_sequence, passphrase);
            ECKeyPair ecKeyPair = Keys.create256r1KeyPair(new SecureRandom(key));
            String adddress = WalletAddress.generate_address((byte) version, ecKeyPair.getPublicKey());
            addreses.add(adddress);
            keypair.add(ecKeyPair);
            TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).store(adddress, new PatriciaTreeNode(BigDecimal.valueOf(1000, 0)));
        }

        int index = 0;
        while (index < transactionSize) {
            for (int i = 0; i < fixedSize - 1; i++) {
                Transaction transaction = new RegularTransaction();
                transaction.setFrom(addreses.get(i));
                transaction.setTo(addreses.get(i + 1));
                transaction.setStatus(StatusType.PENDING);
                transaction.setTimestamp(GetTime.GetTimeStampInString());
                transaction.setZoneFrom(0);
                transaction.setZoneTo(0);
                transaction.setAmount(BigDecimal.valueOf(100));
                transaction.setAmountWithTransactionFee(transaction.getAmount().multiply(BigDecimal.valueOf(10.0 / 100.0)));
                transaction.setNonce(1);
                byte byf[] = serenc.encode(transaction, 1024);
                transaction.setHash(HashUtil.sha256_bytetoString(byf));
                ECDSASignatureData signatureData = ecdsaSign.signSecp256r1Message(transaction.getHash().getBytes(StandardCharsets.UTF_8), keypair.get(i));
                transaction.setSignature(signatureData);
                transactions.add(transaction);
            }
            index++;
        }
        transactionBlock.setTransactionList(transactions);
        // Example usage
        Message message = new Message(
                "Hello ActiveJ with dynamic buffer",
                new byte[]{1, 2, 3, 4, 5}
        );

        // Encode with dynamic sizing
        ByteBuf encodedBuf = dynamicSerializer.encode(transactions.get(0));
        Transaction decodedMessage = dynamicSerializer.decode(encodedBuf);

        byte byf[] = trx_serence.encode(transactions.get(0), 1024);
        Transaction transaction = trx_serence.decode(byf);

        byte byf1[] = trx_serence.encode(transactions.get(0), 1024);
        Transaction transaction1 = trx_serence.decode(byf);

        byte byf2[] = fury.serialize(transactions.get(0));
        Transaction transaction2 = (Transaction) fury.deserialize(byf2);

        BlockSizeCalculator blockSizeCalculator = new BlockSizeCalculator();
        blockSizeCalculator.setTransactionBlock(transactionBlock);
        byte byf3[] = transcion_block.encode(transactionBlock, blockSizeCalculator.TransactionBlockSizeCalculator());
        TransactionBlock transactionBlock1 = (TransactionBlock) transcion_block.decode(byf3);
        length = SerializationUtils.serialize(transactionBlock).length;

    }

    @Test
    public void DynamicSerializerTest1() {
        for (int i = 0; i < transactions.size(); i++) {
            ByteBuf encodedBuf = dynamicSerializer.encode(transactions.get(0));
            Transaction decodedMessage = dynamicSerializer.decode(encodedBuf);
        }
    }

    @Test
    public void AcctiveJTest1() {
        for (int i = 0; i < transactions.size(); i++) {
            byte byf[] = trx_serence.encode(transactions.get(i), 1024);
            Transaction transaction = trx_serence.decode(byf);
        }
    }

    @Test
    public void AcctiveJTest2() {
        for (int i = 0; i < transactions.size(); i++) {
            byte byf[] = trx_serence.encode(transactions.get(i), 1024);
            Transaction transaction = trx_serence.decode(byf);
        }
    }

    @Test
    public void ApacheFuryTest1() {
        for (int i = 0; i < transactions.size(); i++) {
            byte byf[] = fury.serialize(transactions.get(i));
            Transaction transaction = (Transaction) fury.deserialize(byf);
        }
    }

    @Test
    public void ApacheFuryTest2() {
        for (int i = 0; i < transactions.size(); i++) {
            byte byf[] = fury.serialize(transactions.get(i));
            Transaction transaction = (Transaction) fury.deserialize(byf);
        }
    }

    @Test
    public void TransactionBlockFixedLength() {
        for (int i = 0; i < 3; i++) {
            byte byf[] = transcion_block.encode(transactionBlock, length);
            TransactionBlock transactionBlock1 = (TransactionBlock) transcion_block.decode(byf);
        }
    }

    @Test
    public void TransactionBlockDynamicLength() {
        for (int i = 0; i < 3; i++) {
            BlockSizeCalculator blockSizeCalculator = new BlockSizeCalculator();
            blockSizeCalculator.setTransactionBlock(transactionBlock);
            byte byf[] = transcion_block.encode(transactionBlock, blockSizeCalculator.TransactionBlockSizeCalculator());
            TransactionBlock transactionBlock1 = (TransactionBlock) transcion_block.decode(byf);
        }
    }

    @Test
    public void ApacheFuryransactionBlock() {
        for (int i = 0; i < 3; i++) {
            byte byf[] = fury.serialize(transactionBlock);
            TransactionBlock transactionBlock1 = (TransactionBlock) fury.deserialize(byf);
        }
    }

    public static class Message {
        public String content;
        public byte[] data;

        public Message() {
        }

        public Message(String content, byte[] data) {
            this.content = content;
            this.data = data;
        }

        @Serialize
        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public void setData(byte[] data) {
            this.data = data;
        }

        @Serialize
        public byte[] getData() {
            return data;
        }
    }

    public static class DynamicSerializer<T> {
        private final BinarySerializer<T> SERIALIZER;

        private static final int INITIAL_BUFFER_SIZE = 1024; // Start small
        private static final int MAX_BUFFER_SIZE = 1024 * 1024; // 1MB safety limit

        public DynamicSerializer(Class<T> cls) {
            var factory = SerializerFactory.builder();
            List<SerializationUtil.Mapping> list = new ArrayList<>();
            list.add(new SerializationUtil.Mapping(BigDecimal.class, ctx -> new BigDecimalSerializer()));
            list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
            list.forEach(val -> factory.with(val.getType(), val.getSerializerDefMapping()));
            SERIALIZER = factory.build().create(cls);
        }

        public ByteBuf encode(T message) {
            // Get buffer from pool with initial size
            ByteBuf buf = ByteBufPool.allocate(INITIAL_BUFFER_SIZE);
            try {
                while (true) {
                    try {
                        // Try to serialize
                        int pos = buf.head();
                        SERIALIZER.encode(buf.toWriteByteBuffer().array(), pos, message);
                        return buf;
                    } catch (ArrayIndexOutOfBoundsException e) {
                        System.out.println(buf.head());
                        //e.printStackTrace();
                        // Buffer too small, double size and retry
                        int newSize = Math.min(buf.toWriteByteBuffer().capacity() * 2, MAX_BUFFER_SIZE);
                        if (newSize == buf.toWriteByteBuffer().capacity()) {
                            throw new IllegalStateException("Message too large, exceeds " + MAX_BUFFER_SIZE);
                        }
                        ByteBuf newBuf = ByteBufPool.allocate(newSize);
                        buf.recycle();
                        buf = newBuf;
                    }
                }
            } catch (Exception e) {
                buf.recycle();
                throw e;
            }
        }

        public T decode(ByteBuf buf) {
            BinaryInput input = new BinaryInput(buf.toReadByteBuffer().array());
            return SERIALIZER.decode(input);
        }
    }
}
