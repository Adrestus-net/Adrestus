package io.Adrestus.core;

import io.Adrestus.core.Resourses.CachedLatestBlocks;
import io.Adrestus.core.Resourses.MemoryPool;
import io.Adrestus.core.Resourses.MemoryTreePool;
import io.Adrestus.core.RingBuffer.handler.transactions.SignatureEventHandler;
import io.Adrestus.core.RingBuffer.publisher.TransactionEventPublisher;
import io.Adrestus.core.Trie.PatriciaTreeNode;
import io.Adrestus.crypto.HashUtil;
import io.Adrestus.crypto.WalletAddress;
import io.Adrestus.crypto.elliptic.ECDSASign;
import io.Adrestus.crypto.elliptic.ECKeyPair;
import io.Adrestus.crypto.elliptic.Keys;
import io.Adrestus.crypto.elliptic.SignatureData;
import io.Adrestus.crypto.mnemonic.Mnemonic;
import io.Adrestus.crypto.mnemonic.Security;
import io.Adrestus.crypto.mnemonic.WordList;
import io.Adrestus.util.GetTime;
import io.Adrestus.util.SerializationUtil;
import org.checkerframework.checker.units.qual.C;
import org.junit.jupiter.api.Test;
import org.spongycastle.util.encoders.Hex;

import java.security.SecureRandom;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BlockTest {

    @Test
    public void block_test() throws Exception {
        AbstractBlock t = new TransactionBlock();
        t.setHash("hash");
        t.accept(new Genesis());
    }

    //@Test
    public void block_test2() {
        DefaultFactory factory = new DefaultFactory(new TransactionBlock(), new CommitteeBlock());
        var genesis = (Genesis) factory.getBlock(BlockType.GENESIS);
        var regural_block = factory.getBlock(BlockType.REGULAR);
        factory.accept(genesis);
        factory.accept(regural_block);
    }

    @Test
    public void commitee_block(){
        SerializationUtil<CommitteeBlock> encode = new SerializationUtil<CommitteeBlock>(CommitteeBlock.class);
        //byte[] buffer = new byte[200];
        // BinarySerializer<DelegateTransaction> serenc = SerializerBuilder.create().build(DelegateTransaction.class);
        CommitteeBlock block=new CommitteeBlock();
        block.setHash("hash1");
        block.setSize(1);
        byte[] buffer = encode.encode(block);

        CommitteeBlock copys = encode.decode(buffer);
        System.out.println(copys.toString());
        assertEquals(copys, block);
    }

    @Test
     public void transaction_block(){
        SerializationUtil<TransactionBlock> encode = new SerializationUtil<TransactionBlock>(TransactionBlock.class);
        //byte[] buffer = new byte[200];
        // BinarySerializer<DelegateTransaction> serenc = SerializerBuilder.create().build(DelegateTransaction.class);
        TransactionBlock block=new TransactionBlock();
        block.setHash("hash1");
        block.setSize(1);
        block.setZone(0);
        byte[] buffer = encode.encode(block);

        TransactionBlock copys = encode.decode(buffer);
        System.out.println(copys.toString());
        assertEquals(copys, block);
    }

    @Test
    public void block_test3() throws Exception {

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
                .mergeEventsAndPassThen(new SignatureEventHandler(SignatureEventHandler.SignatureBehaviorType.SIMPLE_TRANSACTIONS));
        publisher.start();


        SecureRandom random;
        String mnemonic_code = "fd8cee9c1a3f3f57ab51b25740b24341ae093c8f697fde4df948050d3acd1700f6379d716104d2159e4912509c40ac81714d833e93b822e5ba0fadd68d5568a2";
        random = SecureRandom.getInstance("SHA1PRNG", "SUN");
        random.setSeed(Hex.decode(mnemonic_code));

        ECDSASign ecdsaSign = new ECDSASign();

        SerializationUtil<Transaction> serenc = new SerializationUtil<Transaction>(Transaction.class);

        ArrayList<String> addreses = new ArrayList<>();
        ArrayList<ECKeyPair> keypair = new ArrayList<>();
        int version = 0x00;
        int size = 5;
        for (int i = 0; i < size; i++) {
            Mnemonic mnem = new Mnemonic(Security.NORMAL, WordList.ENGLISH);
            char[] mnemonic_sequence = mnem.create();
            char[] passphrase = "p4ssphr4se".toCharArray();
            byte[] key = mnem.createSeed(mnemonic_sequence, passphrase);
            ECKeyPair ecKeyPair = Keys.createEcKeyPair(new SecureRandom(key));
            String adddress = WalletAddress.generate_address((byte) version, ecKeyPair.getPublicKey());
            addreses.add(adddress);
            keypair.add(ecKeyPair);
            MemoryTreePool.getInstance().store(adddress,new PatriciaTreeNode(1000,0));
        }


        for (int i = 0; i < size - 1; i++) {
            Transaction transaction = new RegularTransaction();
            transaction.setFrom(addreses.get(i));
            transaction.setTo(addreses.get(i + 1));
            transaction.setStatus(StatusType.PENDING);
            transaction.setTimestamp(GetTime.GetTimeStampInString());
            transaction.setZoneFrom(0);
            transaction.setZoneTo(0);
            transaction.setAmount(100);
            transaction.setAmountWithTransactionFee(transaction.getAmount() * (10.0 / 100.0));
            transaction.setNonce(1);
            byte byf[] = serenc.encode(transaction);
            transaction.setHash(HashUtil.sha256_bytetoString(byf));

            SignatureData signatureData = ecdsaSign.secp256SignMessage(Hex.decode(transaction.getHash()), keypair.get(i));
            transaction.setSignature(signatureData);
            MemoryPool.getInstance().add(transaction);
           // publisher.publish(transaction);
            Thread.sleep(1000);
            //publisher.publish(transaction);
        }
        publisher.getJobSyncUntilRemainingCapacityZero();
        publisher.close();



        TransactionBlock prevblock=new TransactionBlock();
        CommitteeBlock committeeBlock=new CommitteeBlock();
        committeeBlock.setGeneration(1);
        committeeBlock.setViewID(1);
        prevblock.setHeight(1);
        prevblock.setHash("hash");
        prevblock.getHeaderData().setTimestamp(GetTime.GetTimeStampInString());
        CachedLatestBlocks.getInstance().setCommitteeBlock(committeeBlock);
        CachedLatestBlocks.getInstance().setTransactionBlock(prevblock);
        DefaultFactory factory = new DefaultFactory();
        TransactionBlock transactionBlock=new TransactionBlock();
        var regural_block = factory.getBlock(BlockType.REGULAR);
        transactionBlock.accept(regural_block);
    }
}
