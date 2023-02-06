package io.Adrestus.api;


import static org.junit.jupiter.api.Assertions.assertTrue;

import io.Adrestus.TreeFactory;
import io.Adrestus.Trie.PatriciaTreeNode;
import io.Adrestus.config.AdrestusConfiguration;
import io.Adrestus.config.KademliaConfiguration;
import io.Adrestus.core.CommitteeBlock;
import io.Adrestus.core.RegularTransaction;
import io.Adrestus.core.Resourses.CachedLatestBlocks;
import io.Adrestus.core.Resourses.CachedLeaderIndex;
import io.Adrestus.core.Resourses.CachedZoneIndex;
import io.Adrestus.core.Resourses.MemoryTransactionPool;
import io.Adrestus.core.RingBuffer.handler.transactions.SignatureEventHandler;
import io.Adrestus.core.RingBuffer.publisher.TransactionEventPublisher;
import io.Adrestus.core.StatusType;
import io.Adrestus.core.Transaction;
import io.Adrestus.crypto.HashUtil;
import io.Adrestus.crypto.SecurityAuditProofs;
import io.Adrestus.crypto.WalletAddress;
import io.Adrestus.crypto.bls.model.BLSPrivateKey;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.elliptic.ECDSASign;
import io.Adrestus.crypto.elliptic.ECKeyPair;
import io.Adrestus.crypto.elliptic.Keys;
import io.Adrestus.crypto.elliptic.SignatureData;
import io.Adrestus.crypto.elliptic.mapper.StakingData;
import io.Adrestus.crypto.mnemonic.Mnemonic;
import io.Adrestus.crypto.mnemonic.Security;
import io.Adrestus.crypto.mnemonic.WordList;
import io.Adrestus.p2p.kademlia.common.NettyConnectionInfo;
import io.Adrestus.p2p.kademlia.repository.KademliaData;
import io.Adrestus.util.GetTime;
import io.Adrestus.util.ObjectSizeCalculator;
import io.Adrestus.util.SerializationUtil;
import org.apache.commons.codec.binary.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.spongycastle.util.encoders.Hex;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Unit test for simple App.
 */
public class TransactionStrategyTest
{
    private static BLSPrivateKey sk1;
    private static BLSPublicKey vk1;

    private static BLSPrivateKey sk2;
    private static BLSPublicKey vk2;

    private static ECKeyPair ecKeyPair1, ecKeyPair2;

    @BeforeAll
    public static void setup() throws Exception {
        int version = 0x00;
        ECDSASign ecdsaSign = new ECDSASign();
        sk1 = new BLSPrivateKey(1);
        vk1 = new BLSPublicKey(sk1);

        sk2 = new BLSPrivateKey(2);
        vk2 = new BLSPublicKey(sk2);

        char[] mnemonic1 = "sample sail jungle learn general promote task puppy own conduct green affair ".toCharArray();
        char[] mnemonic2 = "photo monitor cushion indicate civil witness orchard estate online favorite sustain extend".toCharArray();
        char[] passphrases = "p4ssphr4se".toCharArray();

        Mnemonic mnems = new Mnemonic(Security.NORMAL, WordList.ENGLISH);
        byte[] key1 = mnems.createSeed(mnemonic1, passphrases);
        byte[] key2 = mnems.createSeed(mnemonic2, passphrases);
        SecureRandom random = SecureRandom.getInstance(AdrestusConfiguration.ALGORITHM, AdrestusConfiguration.PROVIDER);
        random.setSeed(key1);
        ecKeyPair1 = Keys.createEcKeyPair(random);
        random.setSeed(key2);
        ecKeyPair2 = Keys.createEcKeyPair(random);
        String address1 = WalletAddress.generate_address((byte) version, ecKeyPair1.getPublicKey());
        String address2 = WalletAddress.generate_address((byte) version, ecKeyPair2.getPublicKey());
        SignatureData signatureData1 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(address1)), ecKeyPair1);
        SignatureData signatureData2 = ecdsaSign.secp256SignMessage(HashUtil.sha256(StringUtils.getBytesUtf8(address2)), ecKeyPair2);

        SerializationUtil<Transaction> serenc = new SerializationUtil<Transaction>(Transaction.class);

        CachedZoneIndex.getInstance().setZoneIndex(0);
        TransactionEventPublisher publisher = new TransactionEventPublisher(4096);
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
                .withZoneEventHandler()
                .mergeEventsAndPassThen(new SignatureEventHandler(SignatureEventHandler.SignatureBehaviorType.SIMPLE_TRANSACTIONS));
        publisher.start();
        ArrayList<String> addreses = new ArrayList<>();
        ArrayList<ECKeyPair> keypair = new ArrayList<>();
        int size = 5;
        for (int i = 0; i < size; i++) {
            Mnemonic mnem = new Mnemonic(Security.NORMAL, WordList.ENGLISH);
            char[] mnemonic_sequence = "sample sail jungle learn general promote task puppy own conduct green affair ".toCharArray();
            char[] passphrase = ("p4ssphr4se"+String.valueOf(i)).toCharArray();
            byte[] key = mnem.createSeed(mnemonic_sequence, passphrase);
            SecureRandom randoms = SecureRandom.getInstance(AdrestusConfiguration.ALGORITHM, AdrestusConfiguration.PROVIDER);
            randoms.setSeed(key);
            ECKeyPair ecKeyPair = Keys.createEcKeyPair(randoms);
            String adddress = WalletAddress.generate_address((byte) version, ecKeyPair.getPublicKey());
            addreses.add(adddress);
            keypair.add(ecKeyPair);
            TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).store(adddress, new PatriciaTreeNode(1000, 0));
        }

        for (int i = 0; i < size - 1; i++) {
            serenc = new SerializationUtil<Transaction>(Transaction.class);
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

            byte byf[] = serenc.encode(transaction,1024);
            transaction.setHash(HashUtil.sha256_bytetoString(byf));

            SignatureData signatureData = ecdsaSign.secp256SignMessage(Hex.decode(transaction.getHash()), keypair.get(i));
            transaction.setSignature(signatureData);

            publisher.publish(transaction);
            //publisher.publish(transaction);
        }
        publisher.getJobSyncUntilRemainingCapacityZero();
        CommitteeBlock committeeBlock = new CommitteeBlock();
        committeeBlock.getHeaderData().setTimestamp("2022-11-18 15:01:29.304");
        committeeBlock.getStructureMap().get(0).put(vk1, "192.168.1.106");
        committeeBlock.getStructureMap().get(0).put(vk2, "192.168.1.116");

        committeeBlock.getStakingMap().put(new StakingData(1,10.0), new KademliaData(new SecurityAuditProofs(addreses.get(0), vk1, ecKeyPair1.getPublicKey(), signatureData1), new NettyConnectionInfo("192.168.1.106", KademliaConfiguration.PORT)));
        committeeBlock.getStakingMap().put(new StakingData(2,13.0), new KademliaData(new SecurityAuditProofs(addreses.get(1), vk2, ecKeyPair2.getPublicKey(), signatureData2), new NettyConnectionInfo("192.168.1.116", KademliaConfiguration.PORT)));

        CachedLatestBlocks.getInstance().setCommitteeBlock(committeeBlock);
        CachedLeaderIndex.getInstance().setCommitteePositionLeader(0);
        CachedZoneIndex.getInstance().setZoneIndex(0);
    }
    @Test
    public void test() throws Exception {
        Strategy transactionStrategy=new Strategy(new TransactionStrategy(MemoryTransactionPool.getInstance().getAll()));
        Strategy transactionStrategy1=new Strategy(new TransactionStrategy((Transaction) MemoryTransactionPool.getInstance().getAll().get(0)));
        System.out.println(MemoryTransactionPool.getInstance().getAll().size());
        transactionStrategy1.execute();
        transactionStrategy.execute();
        transactionStrategy.block_until_send();
    }
}