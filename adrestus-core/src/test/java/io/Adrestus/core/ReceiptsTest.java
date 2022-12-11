package io.Adrestus.core;

import io.Adrestus.MemoryTreePool;
import io.Adrestus.Trie.MerkleNode;
import io.Adrestus.Trie.MerkleProofs;
import io.Adrestus.Trie.MerkleTreeImp;
import io.Adrestus.Trie.PatriciaTreeNode;
import io.Adrestus.config.AdrestusConfiguration;
import io.Adrestus.core.Resourses.MemoryTransactionPool;
import io.Adrestus.core.RingBuffer.handler.transactions.SignatureEventHandler;
import io.Adrestus.core.RingBuffer.publisher.TransactionEventPublisher;
import io.Adrestus.crypto.HashUtil;
import io.Adrestus.crypto.WalletAddress;
import io.Adrestus.crypto.bls.BLS381.ECP;
import io.Adrestus.crypto.bls.BLS381.ECP2;
import io.Adrestus.crypto.bls.mapper.ECP2mapper;
import io.Adrestus.crypto.bls.mapper.ECPmapper;
import io.Adrestus.crypto.elliptic.ECDSASign;
import io.Adrestus.crypto.elliptic.ECKeyPair;
import io.Adrestus.crypto.elliptic.Keys;
import io.Adrestus.crypto.elliptic.SignatureData;
import io.Adrestus.crypto.elliptic.mapper.BigIntegerSerializer;
import io.Adrestus.crypto.elliptic.mapper.CustomSerializerTreeMap;
import io.Adrestus.crypto.mnemonic.Mnemonic;
import io.Adrestus.crypto.mnemonic.Security;
import io.Adrestus.crypto.mnemonic.WordList;
import io.Adrestus.util.GetTime;
import io.Adrestus.util.SerializationUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReceiptsTest {

    private static SerializationUtil<AbstractBlock> serenc;

    @BeforeAll
    public static void setup() throws Exception {
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
        list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        list.add(new SerializationUtil.Mapping(TreeMap.class, ctx -> new CustomSerializerTreeMap()));
        serenc = new SerializationUtil<AbstractBlock>(AbstractBlock.class, list);
        TransactionEventPublisher publisher = new TransactionEventPublisher(1024);

        publisher
                .withAddressSizeEventHandler()
                .withAmountEventHandler()
                .withDelegateEventHandler()
                .withDoubleSpendEventHandler()
                .withHashEventHandler()
                .withNonceEventHandler()
                .withReplayEventHandler()
                .withRewardEventHandler()
                .withStakingEventHandler()
                .withTransactionFeeEventHandler()
                .withTimestampEventHandler()
                .withSameOriginEventHandler()
                .mergeEventsAndPassThen(new SignatureEventHandler(SignatureEventHandler.SignatureBehaviorType.SIMPLE_TRANSACTIONS));
        publisher.start();

        SecureRandom random;
        String mnemonic_code = "fd8cee9c1a3f3f57ab51b25740b24341ae093c8f697fde4df948050d3acd1700f6379d716104d2159e4912509c40ac81714d833e93b822e5ba0fadd68d5568a2";
        random = SecureRandom.getInstance(AdrestusConfiguration.ALGORITHM, AdrestusConfiguration.PROVIDER);
        random.setSeed(Hex.decode(mnemonic_code));

        ECDSASign ecdsaSign = new ECDSASign();

        SerializationUtil<Transaction> serenc = new SerializationUtil<Transaction>(Transaction.class);

        ArrayList<String> addreses = new ArrayList<>();
        ArrayList<ECKeyPair> keypair = new ArrayList<>();
        int version = 0x00;
        int size = 10;
        for (int i = 0; i < size; i++) {
            Mnemonic mnem = new Mnemonic(Security.NORMAL, WordList.ENGLISH);
            char[] mnemonic_sequence = mnem.create();
            char[] passphrase = "p4ssphr4se".toCharArray();
            byte[] key = mnem.createSeed(mnemonic_sequence, passphrase);
            ECKeyPair ecKeyPair = Keys.createEcKeyPair(new SecureRandom(key));
            String adddress = WalletAddress.generate_address((byte) version, ecKeyPair.getPublicKey());
            addreses.add(adddress);
            keypair.add(ecKeyPair);
            MemoryTreePool.getInstance().store(adddress, new PatriciaTreeNode(1000, 0));
        }


        int j = 1;
        for (int i = 0; i < size - 1; i++) {
            Transaction transaction = new RegularTransaction();
            transaction.setFrom(addreses.get(i));
            transaction.setTo(addreses.get(i + 1));
            transaction.setStatus(StatusType.PENDING);
            transaction.setTimestamp(GetTime.GetTimeStampInString());
            transaction.setZoneFrom(0);
            transaction.setZoneTo(j);
            transaction.setAmount(i);
            transaction.setAmountWithTransactionFee(transaction.getAmount() * (10.0 / 100.0));
            transaction.setNonce(1);
            byte byf[] = serenc.encode(transaction);
            transaction.setHash(HashUtil.sha256_bytetoString(byf));
            //  await().atMost(500, TimeUnit.MILLISECONDS);

            SignatureData signatureData = ecdsaSign.secp256SignMessage(Hex.decode(transaction.getHash()), keypair.get(i));
            transaction.setSignature(signatureData);
            //MemoryPool.getInstance().add(transaction);
            publisher.publish(transaction);
            //await().atMost(1000, TimeUnit.MILLISECONDS);
            if (j == 3)
                j = 0;
            j++;
        }
        publisher.getJobSyncUntilRemainingCapacityZero();
        publisher.close();
    }

    @Test
    public void serialize_test() {
        TransactionBlock transactionBlock = new TransactionBlock();
        transactionBlock.setGeneration(4);
        List<Receipt> receiptList = new ArrayList<>();
        receiptList.add(new Receipt(0, 1));
        receiptList.add(new Receipt(0, 2));
        receiptList.add(new Receipt(0, 3));
        receiptList.get(0).getReceiptProofs().add(new Receipt.ReceiptProofs(new RegularTransaction(""), 4, 3, 2, new Receipt.ReceiptData("", 0.0), new MerkleProofs()));
        OutBoundRelay outBoundRelay = new OutBoundRelay(receiptList, "OriginalRootHash");
        transactionBlock.setOutbound(outBoundRelay);

        byte[] buffer = serenc.encode(transactionBlock);
        TransactionBlock clone = (TransactionBlock) serenc.decode(buffer);
        assertEquals(transactionBlock, clone);
    }

    @Test
    public void general_test() throws Exception {
        MerkleTreeImp tree = new MerkleTreeImp();
        TransactionBlock transactionBlock = new TransactionBlock();
        transactionBlock.setGeneration(4);
        transactionBlock.setHeight(100);
        transactionBlock.setTransactionList(MemoryTransactionPool.getInstance().getAll());
        ArrayList<MerkleNode> merkleNodeArrayList = new ArrayList<>();
        transactionBlock.getTransactionList().stream().forEach(x -> {
            merkleNodeArrayList.add(new MerkleNode(x.getHash()));
        });
        tree.my_generate2(merkleNodeArrayList);
        String OriginalRootHash = tree.getRootHash();
        List<Receipt> receiptList = new ArrayList<>();
        receiptList.add(new Receipt(0, 1));
        receiptList.add(new Receipt(0, 2));
        receiptList.add(new Receipt(0, 3));
        for (int i = 0; i < transactionBlock.getTransactionList().size(); i++) {
            Transaction transaction = transactionBlock.getTransactionList().get(i);
            MerkleNode node = new MerkleNode(transaction.getHash());
            tree.build_proofs2(merkleNodeArrayList, node);
            int effective_final = i;
            Optional<Receipt> data = receiptList.stream().filter(val -> val.getZoneTo() == transactionBlock.getTransactionList().get(effective_final).getZoneTo()).findFirst();
            if (data.isEmpty())
                continue;
            data
                    .get()
                    .getReceiptProofs()
                    .add(new Receipt.ReceiptProofs(
                            transaction,
                            transactionBlock.getHeight(),
                            transactionBlock.getGeneration(),
                            i,
                            new Receipt.ReceiptData(transaction.getTo(), transaction.getAmount()), tree.getMerkleeproofs()));

        }
        assertEquals(OriginalRootHash, tree.GenerateRoot(receiptList.get(0).getReceiptProofs().get(0).getProofs()));
        assertEquals(OriginalRootHash, tree.GenerateRoot(receiptList.get(0).getReceiptProofs().get(1).getProofs()));
        assertEquals(OriginalRootHash, tree.GenerateRoot(receiptList.get(1).getReceiptProofs().get(0).getProofs()));
        assertEquals(OriginalRootHash, tree.GenerateRoot(receiptList.get(1).getReceiptProofs().get(1).getProofs()));
        assertEquals(OriginalRootHash, tree.GenerateRoot(receiptList.get(2).getReceiptProofs().get(0).getProofs()));
        assertEquals(OriginalRootHash, tree.GenerateRoot(receiptList.get(2).getReceiptProofs().get(1).getProofs()));

        OutBoundRelay outBoundRelay = new OutBoundRelay(receiptList, OriginalRootHash);
        transactionBlock.setOutbound(outBoundRelay);

        byte[] buffer = serenc.encode(transactionBlock);
        TransactionBlock clone = (TransactionBlock) serenc.decode(buffer);
        assertEquals(transactionBlock, clone);

    }

}
