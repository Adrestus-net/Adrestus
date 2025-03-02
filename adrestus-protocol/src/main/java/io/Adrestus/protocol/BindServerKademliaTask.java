package io.Adrestus.protocol;

import io.Adrestus.config.AdrestusConfiguration;
import io.Adrestus.config.KademliaConfiguration;
import io.Adrestus.config.NodeSettings;
import io.Adrestus.core.BlockIndex;
import io.Adrestus.core.IBlockIndex;
import io.Adrestus.core.Resourses.CachedKademliaNodes;
import io.Adrestus.crypto.HashUtil;
import io.Adrestus.crypto.SecurityAuditProofs;
import io.Adrestus.crypto.WalletAddress;
import io.Adrestus.crypto.bls.model.BLSPrivateKey;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.bls.model.CachedBLSKeyPair;
import io.Adrestus.crypto.bls.model.Params;
import io.Adrestus.crypto.elliptic.ECDSASign;
import io.Adrestus.crypto.elliptic.ECDSASignatureData;
import io.Adrestus.crypto.elliptic.ECKeyPair;
import io.Adrestus.crypto.elliptic.Keys;
import io.Adrestus.crypto.mnemonic.Mnemonic;
import io.Adrestus.crypto.mnemonic.Security;
import io.Adrestus.crypto.mnemonic.WordList;
import io.Adrestus.network.IPFinder;
import io.Adrestus.p2p.kademlia.common.NettyConnectionInfo;
import io.Adrestus.p2p.kademlia.exception.UnsupportedBoundingException;
import io.Adrestus.p2p.kademlia.node.DHTBootstrapNode;
import io.Adrestus.p2p.kademlia.node.DHTRegularNode;
import io.Adrestus.p2p.kademlia.node.KeyHashGenerator;
import io.Adrestus.p2p.kademlia.repository.KademliaData;
import io.Adrestus.p2p.kademlia.util.BoundedHashUtil;
import lombok.SneakyThrows;
import org.apache.commons.codec.binary.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

public class BindServerKademliaTask extends AdrestusTask {

    private static Logger LOG = LoggerFactory.getLogger(BindServerKademliaTask.class);
    private static final int version = 0x00;

    private static final String MNEMONIC = "user.mnemonic";
    private static final String PASSPHRASE = "user.passphrace";
    private final IBlockIndex blockIndex;
    private final KeyHashGenerator<BigInteger, String> keyHashGenerator;
    private final String ip;
    private final NettyConnectionInfo bootstrapNettyConnectionInfo, nettyConnectionInfo;

    private KademliaData kademliaData;

    private DHTBootstrapNode dhtBootstrapNode;
    private DHTRegularNode dhtRegularNode;

    static {
        NodeSettings.getInstance();
    }

    public BindServerKademliaTask() {
        this.ip = IPFinder.getLocalIP();
        this.blockIndex = new BlockIndex();
        this.bootstrapNettyConnectionInfo = new NettyConnectionInfo(KademliaConfiguration.BOOTSTRAP_NODE_IP, KademliaConfiguration.BootstrapNodePORT);
        this.nettyConnectionInfo = new NettyConnectionInfo(this.ip, KademliaConfiguration.PORT);
        this.keyHashGenerator = key -> {
            try {
                return new BoundedHashUtil(NodeSettings.getInstance().getIdentifierSize()).hash(new BigInteger(HashUtil.convertIPtoHex(key, 1)), BigInteger.class);
            } catch (UnsupportedBoundingException e) {
                throw new IllegalArgumentException("Key hash generator not valid");
            }
        };
        this.dhtBootstrapNode = new DHTBootstrapNode(
                this.bootstrapNettyConnectionInfo,
                KademliaConfiguration.BootstrapNodeID,
                keyHashGenerator);

        this.dhtRegularNode = new DHTRegularNode(this.nettyConnectionInfo, new BigInteger(HashUtil.convertIPtoHex(IPFinder.getLocalIP(), 24)), keyHashGenerator);
        this.InitKademliaData();
    }

    public BindServerKademliaTask(String mnemonic, String passphrase) {
        this.ip = IPFinder.getLocalIP();
        this.blockIndex = new BlockIndex();
        this.bootstrapNettyConnectionInfo = new NettyConnectionInfo(KademliaConfiguration.BOOTSTRAP_NODE_IP, KademliaConfiguration.BootstrapNodePORT);
        this.nettyConnectionInfo = new NettyConnectionInfo(this.ip, KademliaConfiguration.PORT);
        this.keyHashGenerator = key -> {
            try {
                return new BoundedHashUtil(NodeSettings.getInstance().getIdentifierSize()).hash(new BigInteger(HashUtil.convertIPtoHex(key, 1)), BigInteger.class);
            } catch (UnsupportedBoundingException e) {
                throw new IllegalArgumentException("Key hash generator not valid");
            }
        };
        this.dhtBootstrapNode = new DHTBootstrapNode(
                this.bootstrapNettyConnectionInfo,
                KademliaConfiguration.BootstrapNodeID,
                keyHashGenerator);
        this.dhtRegularNode = new DHTRegularNode(this.nettyConnectionInfo, new BigInteger(HashUtil.convertIPtoHex(IPFinder.getLocalIP(), 24)), keyHashGenerator);
        this.InitKademliaData(mnemonic, passphrase);
    }

    public BindServerKademliaTask(ECKeyPair keypair, BLSPublicKey blsPublicKey) {
        this.ip = IPFinder.getLocalIP();
        this.blockIndex = new BlockIndex();
        this.bootstrapNettyConnectionInfo = new NettyConnectionInfo(KademliaConfiguration.BOOTSTRAP_NODE_IP, KademliaConfiguration.BootstrapNodePORT);
        this.nettyConnectionInfo = new NettyConnectionInfo(this.ip, KademliaConfiguration.PORT);
        this.keyHashGenerator = key -> {
            try {
                return new BoundedHashUtil(NodeSettings.getInstance().getIdentifierSize()).hash(new BigInteger(HashUtil.convertIPtoHex(key, 1)), BigInteger.class);
            } catch (UnsupportedBoundingException e) {
                throw new IllegalArgumentException("Key hash generator not valid");
            }
        };
        this.dhtBootstrapNode = new DHTBootstrapNode(
                this.bootstrapNettyConnectionInfo,
                KademliaConfiguration.BootstrapNodeID,
                keyHashGenerator);
        this.dhtRegularNode = new DHTRegularNode(this.nettyConnectionInfo, new BigInteger(HashUtil.convertIPtoHex(IPFinder.getLocalIP(), 24)), keyHashGenerator);
        this.InitKademliaData(keypair, blsPublicKey);
    }


    @SneakyThrows
    public void InitKademliaData() {
        try {
            final ECDSASign ecdsaSign = new ECDSASign();
            final Mnemonic mnem = new Mnemonic(Security.NORMAL, WordList.ENGLISH);
            final SecureRandom random = SecureRandom.getInstance(AdrestusConfiguration.ALGORITHM, AdrestusConfiguration.PROVIDER);
            byte[] key1 = mnem.createSeed(CachedConfigurationProperties.getInstance().getProp().getProperty(MNEMONIC).toCharArray(), CachedConfigurationProperties.getInstance().getProp().getProperty(PASSPHRASE).toCharArray());
            random.setSeed(key1);
            final ECKeyPair ecKeyPair = Keys.create256r1KeyPair(random);
            final String address = WalletAddress.generate_address((byte) version, ecKeyPair.getPublicKey());
            final ECDSASignatureData signatureData = ecdsaSign.signSecp256r1Message(HashUtil.sha256(StringUtils.getBytesUtf8(address)), ecKeyPair);
            final BLSPrivateKey sk = new BLSPrivateKey(random);
            final BLSPublicKey vk = new BLSPublicKey(sk, new Params(CachedConfigurationProperties.getInstance().getProp().getProperty(PASSPHRASE).getBytes(StandardCharsets.UTF_8)));
            if (ip.equals(KademliaConfiguration.BOOTSTRAP_NODE_IP))
                this.kademliaData = new KademliaData(new SecurityAuditProofs(address, vk, ecKeyPair.getPublicKey(), signatureData), bootstrapNettyConnectionInfo);
            else
                this.kademliaData = new KademliaData(new SecurityAuditProofs(address, vk, ecKeyPair.getPublicKey(), signatureData), nettyConnectionInfo);

            CachedBLSKeyPair.getInstance().setPrivateKey(sk);
            CachedBLSKeyPair.getInstance().setPublicKey(vk);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SneakyThrows
    public void InitKademliaData(String mnemonic, String passphrase) {
        try {
            final ECDSASign ecdsaSign = new ECDSASign();
            final Mnemonic mnem = new Mnemonic(Security.NORMAL, WordList.ENGLISH);
            final SecureRandom secureRandom = SecureRandom.getInstance(AdrestusConfiguration.ALGORITHM, AdrestusConfiguration.PROVIDER);
            final byte[] key = mnem.createSeed(mnemonic.toCharArray(), passphrase.toCharArray());
            secureRandom.setSeed(key);
            final ECKeyPair ecKeyPair = Keys.create256r1KeyPair(secureRandom);
            final String address = WalletAddress.generate_address((byte) version, ecKeyPair.getPublicKey());
            final ECDSASignatureData signatureData = ecdsaSign.signSecp256r1Message(HashUtil.sha256(StringUtils.getBytesUtf8(address)), ecKeyPair);
            final BLSPrivateKey sk = new BLSPrivateKey(secureRandom);
            final BLSPublicKey vk = new BLSPublicKey(sk);
            //final BLSPublicKey vk = new BLSPublicKey(sk, new Params(passphrase.getBytes(StandardCharsets.UTF_8)));
            if (ip.equals(KademliaConfiguration.BOOTSTRAP_NODE_IP))
                this.kademliaData = new KademliaData(new SecurityAuditProofs(address, vk, ecKeyPair.getPublicKey(), signatureData), bootstrapNettyConnectionInfo);
            else
                this.kademliaData = new KademliaData(new SecurityAuditProofs(address, vk, ecKeyPair.getPublicKey(), signatureData), nettyConnectionInfo);

            CachedBLSKeyPair.getInstance().setPrivateKey(sk);
            CachedBLSKeyPair.getInstance().setPublicKey(vk);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void InitKademliaData(ECKeyPair ecKeyPair, BLSPublicKey blsPublicKey) {
        try {
            final ECDSASign ecdsaSign = new ECDSASign();
            final String address = WalletAddress.generate_address((byte) version, ecKeyPair.getPublicKey());
            final ECDSASignatureData signatureData = ecdsaSign.signSecp256r1Message(HashUtil.sha256(StringUtils.getBytesUtf8(address)), ecKeyPair);
            if (ip.equals(KademliaConfiguration.BOOTSTRAP_NODE_IP))
                this.kademliaData = new KademliaData(new SecurityAuditProofs(address, blsPublicKey, ecKeyPair.getPublicKey(), signatureData), bootstrapNettyConnectionInfo);
            else
                this.kademliaData = new KademliaData(new SecurityAuditProofs(address, blsPublicKey, ecKeyPair.getPublicKey(), signatureData), nettyConnectionInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void execute() {
        try {
            if (this.ip.equals(KademliaConfiguration.BOOTSTRAP_NODE_IP)) {
                dhtBootstrapNode.setKademliaData(kademliaData);
                dhtBootstrapNode.start();
                dhtBootstrapNode.scheduledFuture(KademliaConfiguration.KADEMLIA_ROUTING_TABLE_DELAY);
                CachedKademliaNodes.getInstance().setDhtBootstrapNode(dhtBootstrapNode);
            } else {
                dhtBootstrapNode.Init();
                dhtRegularNode.setKademliaData(kademliaData);
                dhtRegularNode.start(dhtBootstrapNode);
                dhtRegularNode.scheduledFuture(KademliaConfiguration.KADEMLIA_ROUTING_TABLE_DELAY);
                CachedKademliaNodes.getInstance().setDhtRegularNode(dhtRegularNode);
            }
            LOG.info("execute");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SneakyThrows
    @Override
    public void close() {
        try {
            if (ip.equals(KademliaConfiguration.BOOTSTRAP_NODE_IP)) {
                dhtBootstrapNode.close();
                dhtBootstrapNode = null;
            } else {
                dhtRegularNode.close();
                dhtRegularNode = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
