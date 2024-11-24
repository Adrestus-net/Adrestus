package io.Adrestus.core;

import com.google.common.reflect.TypeToken;
import io.Adrestus.TreeFactory;
import io.Adrestus.Trie.MerkleNode;
import io.Adrestus.Trie.MerkleTreeOptimizedImp;
import io.Adrestus.Trie.PatriciaTreeNode;
import io.Adrestus.config.AdrestusConfiguration;
import io.Adrestus.config.KafkaConfiguration;
import io.Adrestus.core.Resourses.CachedZoneIndex;
import io.Adrestus.core.Util.BlockSizeCalculator;
import io.Adrestus.crypto.HashUtil;
import io.Adrestus.crypto.WalletAddress;
import io.Adrestus.crypto.bls.BLS381.ECP;
import io.Adrestus.crypto.bls.BLS381.ECP2;
import io.Adrestus.crypto.bls.BLSSignatureData;
import io.Adrestus.crypto.bls.mapper.ECP2mapper;
import io.Adrestus.crypto.bls.mapper.ECPmapper;
import io.Adrestus.crypto.bls.model.BLSPrivateKey;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.bls.model.BLSSignature;
import io.Adrestus.crypto.elliptic.ECDSASign;
import io.Adrestus.crypto.elliptic.ECDSASignatureData;
import io.Adrestus.crypto.elliptic.ECKeyPair;
import io.Adrestus.crypto.elliptic.Keys;
import io.Adrestus.crypto.elliptic.mapper.BigDecimalSerializer;
import io.Adrestus.crypto.elliptic.mapper.BigIntegerSerializer;
import io.Adrestus.crypto.elliptic.mapper.CustomSerializerTreeMap;
import io.Adrestus.crypto.mnemonic.Mnemonic;
import io.Adrestus.crypto.mnemonic.Security;
import io.Adrestus.crypto.mnemonic.WordList;
import io.Adrestus.erasure.code.ArrayDataDecoder;
import io.Adrestus.erasure.code.ArrayDataEncoder;
import io.Adrestus.erasure.code.EncodingPacket;
import io.Adrestus.erasure.code.OpenRQ;
import io.Adrestus.erasure.code.decoder.SourceBlockDecoder;
import io.Adrestus.erasure.code.encoder.SourceBlockEncoder;
import io.Adrestus.erasure.code.parameters.FECParameterObject;
import io.Adrestus.erasure.code.parameters.FECParameters;
import io.Adrestus.erasure.code.parameters.FECParametersPreConditions;
import io.Adrestus.network.ConsensusBroker;
import io.Adrestus.network.TopicType;
import io.Adrestus.util.GetTime;
import io.Adrestus.util.SerializationUtil;
import io.distributedLedger.*;
import lombok.SneakyThrows;
import org.apache.commons.codec.binary.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.spongycastle.util.encoders.Hex;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

public class KafkaTransactionBlockTest {
    private static ArrayList<String> addreses = new ArrayList<>();
    private static ArrayList<ECKeyPair> keypair = new ArrayList<>();
    private static ArrayList<Transaction> transactions = new ArrayList<>();
    private static SerializationUtil<AbstractBlock> serenc;
    private static SerializationUtil<Transaction> trx_serence;
    private static SerializationUtil<SerializableErasureObject> serenc_erasure;
    private static SerializationUtil<ArrayList<byte[]>> serenc__final_erasure;
    private static TransactionBlock transactionBlock;

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
    private static ECDSASignatureData signatureData1, signatureData2, signatureData3;
    private static Callback transactionCallback;
    private static int version = 0x00;
    private static int size = 10;


    private static final int VIEW_NUMBER = 1;

    private static ArrayList<String> ips;
    private static ConsensusBroker consensusBroker;
    private static OptionalInt position;

    public static void delete_test() {
        IDatabase<String, TransactionBlock> transaction_block1 = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, DatabaseInstance.ZONE_0_TRANSACTION_BLOCK);
        IDatabase<String, TransactionBlock> transaction_block2 = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, DatabaseInstance.ZONE_1_TRANSACTION_BLOCK);
        IDatabase<String, TransactionBlock> transaction_block3 = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, DatabaseInstance.ZONE_2_TRANSACTION_BLOCK);
        IDatabase<String, TransactionBlock> transaction_block4 = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, DatabaseInstance.ZONE_3_TRANSACTION_BLOCK);

        IDatabase<String, byte[]> patricia_tree0 = new DatabaseFactory(String.class, byte[].class).getDatabase(DatabaseType.ROCKS_DB, PatriciaTreeInstance.PATRICIA_TREE_INSTANCE_0);
        IDatabase<String, byte[]> patricia_tree1 = new DatabaseFactory(String.class, byte[].class).getDatabase(DatabaseType.ROCKS_DB, PatriciaTreeInstance.PATRICIA_TREE_INSTANCE_1);
        IDatabase<String, byte[]> patricia_tree2 = new DatabaseFactory(String.class, byte[].class).getDatabase(DatabaseType.ROCKS_DB, PatriciaTreeInstance.PATRICIA_TREE_INSTANCE_2);
        IDatabase<String, byte[]> patricia_tree3 = new DatabaseFactory(String.class, byte[].class).getDatabase(DatabaseType.ROCKS_DB, PatriciaTreeInstance.PATRICIA_TREE_INSTANCE_3);

        IDatabase<String, CommitteeBlock> commit = new DatabaseFactory(String.class, CommitteeBlock.class).getDatabase(DatabaseType.ROCKS_DB, DatabaseInstance.COMMITTEE_BLOCK);


        //ITS IMPORTANT FIRST DELETE PATRICIA TREE AND AFTER TRASNACTION BLOCKS
        patricia_tree0.delete_db();
        patricia_tree1.delete_db();
        patricia_tree2.delete_db();
        patricia_tree3.delete_db();

        transaction_block1.delete_db();
        transaction_block2.delete_db();
        transaction_block3.delete_db();
        transaction_block4.delete_db();


        commit.delete_db();
    }

    @SneakyThrows
    @BeforeAll
    public static void setup() {
        if (System.out.getClass().getName().contains("maven")) {
            System.out.println("Running from Maven: ");
            return;
        }

        delete_test();
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

        sizeCalculator = new BlockSizeCalculator();
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(BigDecimal.class, ctx -> new BigDecimalSerializer()));
        list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
        list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        list.add(new SerializationUtil.Mapping(TreeMap.class, ctx -> new CustomSerializerTreeMap()));
        serenc = new SerializationUtil<AbstractBlock>(AbstractBlock.class, list);
        serenc_erasure = new SerializationUtil<SerializableErasureObject>(SerializableErasureObject.class, list);
        serenc__final_erasure = new SerializationUtil<ArrayList<byte[]>>(new TypeToken<List<byte[]>>() {
        }.getType());
        SecureRandom random;
        String mnemonic_code = "fd8cee9c1a3f3f57ab51b25740b24341ae093c8f697fde4df948050d3acd1700f6379d716104d2159e4912509c40ac81714d833e93b822e5ba0fadd68d5568a2";
        random = SecureRandom.getInstance(AdrestusConfiguration.ALGORITHM, AdrestusConfiguration.PROVIDER);
        random.setSeed(Hex.decode(mnemonic_code));

        ECDSASign ecdsaSign = new ECDSASign();

        List<SerializationUtil.Mapping> list2 = new ArrayList<>();
        list2.add(new SerializationUtil.Mapping(BigDecimal.class, ctx -> new BigDecimalSerializer()));
        list2.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        trx_serence = new SerializationUtil<Transaction>(Transaction.class, list2);

        for (int i = 0; i < 10; i++) {
            Mnemonic mnem = new Mnemonic(Security.NORMAL, WordList.ENGLISH);
            char[] mnemonic_sequence = mnem.create();
            char[] passphrase = "p4ssphr4se".toCharArray();
            byte[] key = mnem.createSeed(mnemonic_sequence, passphrase);
            ECKeyPair ecKeyPair = Keys.createEcKeyPair(new SecureRandom(key));
            String adddress = WalletAddress.generate_address((byte) version, ecKeyPair.getPublicKey());
            addreses.add(adddress);
            keypair.add(ecKeyPair);
            TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).store(adddress, new PatriciaTreeNode(BigDecimal.valueOf(1000), 0));
        }

        signatureData1 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(addreses.get(0))), keypair.get(0));
        signatureData2 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(addreses.get(1))), keypair.get(1));
        signatureData3 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(addreses.get(2))), keypair.get(2));

        transactionCallback = new TransactionCallback();

        for (int j = 0; j < size; j++) {
            for (int i = 0; i < 10 - 1; i++) {
                Transaction transaction = new RegularTransaction();
                transaction.setFrom(addreses.get(i));
                transaction.setTo(addreses.get(i + 1));
                transaction.setStatus(StatusType.PENDING);
                transaction.setTimestamp(GetTime.GetTimeStampInString());
                transaction.setZoneFrom(0);
                transaction.setZoneTo(0);
                transaction.setAmount(BigDecimal.valueOf(100));
                transaction.setAmountWithTransactionFee(transaction.getAmount().multiply(BigDecimal.valueOf(j + 1 / 100.0)));
                transaction.setNonce(j);
                transaction.setTransactionCallback(transactionCallback);
                byte byf[] = trx_serence.encode(transaction, 1024);
                transaction.setHash(HashUtil.sha256_bytetoString(byf));
                await().atMost(10, TimeUnit.MILLISECONDS);

                ECDSASignatureData signatureData = ecdsaSign.secp256SignMessage(Hex.decode(transaction.getHash()), keypair.get(i));
                transaction.setSignature(signatureData);
                transactions.add(transaction);
            }
        }
        transactionBlock = new TransactionBlock();
        transactionBlock.setHash("hash");
        transactionBlock.setZone(1);
        transactionBlock.setViewID(1);
        transactionBlock.setHeight(1);
        transactionBlock.setTransactionList(transactions);

        HashMap<BLSPublicKey, BLSSignatureData> hashMap = new HashMap<BLSPublicKey, BLSSignatureData>();
        BLSSignatureData blsSignatureData1 = new BLSSignatureData();
        blsSignatureData1.getSignature()[0] = BLSSignature.sign("0".getBytes(StandardCharsets.UTF_8), sk1);
        blsSignatureData1.getSignature()[1] = BLSSignature.sign("1".getBytes(StandardCharsets.UTF_8), sk1);
        blsSignatureData1.getMessageHash()[0] = "0";
        blsSignatureData1.getMessageHash()[1] = "1";
        BLSSignatureData blsSignatureData2 = new BLSSignatureData();
        blsSignatureData2.getSignature()[0] = BLSSignature.sign("0".getBytes(StandardCharsets.UTF_8), sk2);
        blsSignatureData2.getSignature()[1] = BLSSignature.sign("1".getBytes(StandardCharsets.UTF_8), sk2);
        blsSignatureData2.getMessageHash()[0] = "0";
        blsSignatureData2.getMessageHash()[1] = "1";
        BLSSignatureData blsSignatureData3 = new BLSSignatureData();
        blsSignatureData3.getSignature()[0] = BLSSignature.sign("0".getBytes(StandardCharsets.UTF_8), sk3);
        blsSignatureData3.getSignature()[1] = BLSSignature.sign("1".getBytes(StandardCharsets.UTF_8), sk3);
        blsSignatureData3.getMessageHash()[0] = "0";
        blsSignatureData3.getMessageHash()[1] = "1";
        BLSSignatureData blsSignatureData4 = new BLSSignatureData();
        blsSignatureData4.getSignature()[0] = BLSSignature.sign("0".getBytes(StandardCharsets.UTF_8), sk4);
        blsSignatureData4.getSignature()[1] = BLSSignature.sign("1".getBytes(StandardCharsets.UTF_8), sk4);
        blsSignatureData4.getMessageHash()[0] = "0";
        blsSignatureData4.getMessageHash()[1] = "1";
        hashMap.put(vk1, blsSignatureData1);
        hashMap.put(vk2, blsSignatureData2);
        hashMap.put(vk3, blsSignatureData3);
        hashMap.put(vk4, blsSignatureData4);
        transactionBlock.AddAllSignatureData(hashMap);

        Socket socket = new Socket();
        socket.connect(new InetSocketAddress("google.com", 80));
        String IP = socket.getLocalAddress().getHostAddress();
        KafkaConfiguration.KAFKA_HOST = IP;
        ips = new ArrayList<>();
        ips.add("192.168.1.106");
        ips.add("192.168.1.116");
        ips.add("192.168.1.115");
        position = IntStream.range(0, list.size()).filter(i -> IP.equals(ips.get(i))).findFirst();
        consensusBroker = new ConsensusBroker(ips, ips.getFirst(), position.getAsInt());
        consensusBroker.initializeKafkaKingdom();
    }

    @SneakyThrows
    @Test
    public void KafkaDisperseTest() {
        if (System.out.getClass().getName().contains("maven")) {
            System.out.println("Running from Maven: ");
            return;
        }

        if (position.getAsInt() == 0) {
            BlockSizeCalculator sizeCalculator = new BlockSizeCalculator();
            sizeCalculator.setTransactionBlock(transactionBlock);
            byte[] buffer = serenc.encode(transactionBlock, sizeCalculator.TransactionBlockSizeCalculator());

            long dataLen = buffer.length;
            int sizeOfCommittee = 2;
            double loss = .6;
            int numSrcBlks = sizeOfCommittee;
            int symbSize = (int) (dataLen / sizeOfCommittee);
            FECParameterObject object = FECParametersPreConditions.CalculateFECParameters(dataLen, symbSize, numSrcBlks);
            FECParameters fecParams = FECParameters.newParameters(object.getDataLen(), object.getSymbolSize(), object.getNumberOfSymbols());

            byte[] data = new byte[fecParams.dataLengthAsInt()];
            System.arraycopy(buffer, 0, data, 0, data.length);
            final ArrayDataEncoder enc = OpenRQ.newEncoder(data, fecParams);
            ArrayList<SerializableErasureObject> serializableErasureObjects = new ArrayList<SerializableErasureObject>();
            ArrayList<EncodingPacket> n = new ArrayList<EncodingPacket>();
            for (SourceBlockEncoder sbEnc : enc.sourceBlockIterable()) {
                for (EncodingPacket srcPacket : sbEnc.sourcePacketsIterable()) {
                    n.add(srcPacket);
                }
            }
            MerkleTreeOptimizedImp tree = new MerkleTreeOptimizedImp();
            ArrayList<MerkleNode> merkleNodes = new ArrayList<MerkleNode>();
            for (int i = 0; i < n.size(); i++) {
                SerializableErasureObject serializableErasureObject = new SerializableErasureObject(object, n.get(i).asArray(), new ArrayList<byte[]>());
                serializableErasureObjects.add(serializableErasureObject);
                merkleNodes.add(new MerkleNode(HashUtil.XXH3(serializableErasureObject.getOriginalPacketChunks())));
            }
            tree.constructTree(merkleNodes);
            String original_hash = tree.getRootHash();
            for (int j = 0; j < serializableErasureObjects.size(); j++) {
                tree.build_proofs(new MerkleNode(HashUtil.XXH3(serializableErasureObjects.get(j).getOriginalPacketChunks())));
                serializableErasureObjects.get(j).setProofs(tree.getMerkleeproofs());
                serializableErasureObjects.get(j).setRootMerkleHash(tree.getRootHash());
            }
            int sendSize = 0;
            int onlyFirstSize = 0;
            if (serializableErasureObjects.size() >= sizeOfCommittee) {
                sendSize = serializableErasureObjects.size() / sizeOfCommittee;
                onlyFirstSize = (n.size() - sendSize * sizeOfCommittee);
            } else {
                sendSize = sizeOfCommittee;
                onlyFirstSize = sizeOfCommittee - sendSize * n.size();
            }

            int startPosition = 0;
            ArrayList<ArrayList<byte[]>> finalList = new ArrayList<>();
            while (startPosition < serializableErasureObjects.size()) {
                int endPosition = Math.min(startPosition + sendSize + onlyFirstSize, serializableErasureObjects.size());
                ArrayList<byte[]> toSend = new ArrayList<>(endPosition - startPosition);
                for (int i = startPosition; i < endPosition; i++) {
                    toSend.add(serenc_erasure.encode(serializableErasureObjects.get(i)));
                }
                if (toSend.isEmpty()) {
                    throw new IllegalArgumentException("Size of toSend is 0");
                }
                finalList.add(toSend);
                startPosition = endPosition;
                onlyFirstSize = 0;
            }
            assertEquals(finalList.size(), sizeOfCommittee);
            consensusBroker.distributeDisperseMessageFromLeader(finalList, String.valueOf(VIEW_NUMBER));
            Thread.sleep(6000);
        } else {
            if (position.getAsInt() == 1) {
                ArrayList<byte[]> fromLeaderReceive = consensusBroker.receiveDisperseHandledMessageFromLeader(TopicType.DISPERSE_PHASE1, String.valueOf(VIEW_NUMBER) + 1);
                assertFalse(fromLeaderReceive.isEmpty());
                System.out.println("received");
                consensusBroker.distributeDisperseMessageToValidators(fromLeaderReceive, String.valueOf(VIEW_NUMBER));
                ArrayList<ArrayList<byte[]>> finalList = consensusBroker.retrieveDisperseMessageFromValidatorsAndConcatResponse(fromLeaderReceive, String.valueOf(VIEW_NUMBER));
                System.out.println("received2");
                assert (!finalList.isEmpty());

                //#########################################################################################################################
                ArrayList<SerializableErasureObject> recserializableErasureObjects = new ArrayList<SerializableErasureObject>();
                for (ArrayList<byte[]> rec_list : finalList) {
                    for (byte[] rec_buff : rec_list) {
                        recserializableErasureObjects.add(serenc_erasure.decode(rec_buff));
                    }
                }

                for (SerializableErasureObject obj : recserializableErasureObjects) {
                    if (!obj.CheckChunksValidity(recserializableErasureObjects.get(0).getRootMerkleHash())) {
                        throw new IllegalArgumentException("Merklee Hash is not valid");
                    }
                }

                Collections.shuffle(recserializableErasureObjects);
                FECParameterObject recobject = recserializableErasureObjects.get(0).getFecParameterObject();
                FECParameters recfecParams = FECParameters.newParameters(recobject.getDataLen(), recobject.getSymbolSize(), recobject.getNumberOfSymbols());
                final ArrayDataDecoder dec = OpenRQ.newDecoder(recfecParams, recobject.getSymbolOverhead());

                for (int i = 0; i < recserializableErasureObjects.size(); i++) {
                    EncodingPacket encodingPacket = dec.parsePacket(ByteBuffer.wrap(recserializableErasureObjects.get(i).getOriginalPacketChunks()), false).value();
                    final SourceBlockDecoder sbDec = dec.sourceBlock(encodingPacket.sourceBlockNumber());
                    sbDec.putEncodingPacket(encodingPacket);
                }

                TransactionBlock copys = (TransactionBlock) serenc.decode(dec.dataArray());
                assertDoesNotThrow(() -> assertNotNull(copys));
                System.out.println("Data is equal");

                Thread.sleep(4000);
            } else {
                ArrayList<byte[]> fromLeaderReceive = consensusBroker.receiveDisperseHandledMessageFromLeader(TopicType.DISPERSE_PHASE1, String.valueOf(VIEW_NUMBER) + 2);
                assertFalse(fromLeaderReceive.isEmpty());
                System.out.println("received");
                consensusBroker.distributeDisperseMessageToValidators(fromLeaderReceive, String.valueOf(VIEW_NUMBER));
                ArrayList<ArrayList<byte[]>> finalList = consensusBroker.retrieveDisperseMessageFromValidatorsAndConcatResponse(fromLeaderReceive, String.valueOf(VIEW_NUMBER));
                System.out.println("received2");
                assert (!finalList.isEmpty());

                //#########################################################################################################################
                ArrayList<SerializableErasureObject> recserializableErasureObjects = new ArrayList<SerializableErasureObject>();
                for (ArrayList<byte[]> rec_list : finalList) {
                    for (byte[] rec_buff : rec_list) {
                        recserializableErasureObjects.add(serenc_erasure.decode(rec_buff));
                    }
                }

                for (SerializableErasureObject obj : recserializableErasureObjects) {
                    if (!obj.CheckChunksValidity(recserializableErasureObjects.get(0).getRootMerkleHash())) {
                        throw new IllegalArgumentException("Merklee Hash is not valid");
                    }
                }

                Collections.shuffle(recserializableErasureObjects);
                FECParameterObject recobject = recserializableErasureObjects.get(0).getFecParameterObject();
                FECParameters recfecParams = FECParameters.newParameters(recobject.getDataLen(), recobject.getSymbolSize(), recobject.getNumberOfSymbols());
                final ArrayDataDecoder dec = OpenRQ.newDecoder(recfecParams, recobject.getSymbolOverhead());

                for (int i = 0; i < recserializableErasureObjects.size(); i++) {
                    EncodingPacket encodingPacket = dec.parsePacket(ByteBuffer.wrap(recserializableErasureObjects.get(i).getOriginalPacketChunks()), false).value();
                    final SourceBlockDecoder sbDec = dec.sourceBlock(encodingPacket.sourceBlockNumber());
                    sbDec.putEncodingPacket(encodingPacket);
                }

                TransactionBlock copys = (TransactionBlock) serenc.decode(dec.dataArray());
                assertDoesNotThrow(() -> assertNotNull(copys));
                System.out.println("Data is equal");

                Thread.sleep(4000);
            }
        }
    }
}
