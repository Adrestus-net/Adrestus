package io.Adrestus.consensus;

import com.google.common.reflect.TypeToken;
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
import io.Adrestus.crypto.vdf.VDFMessage;
import io.Adrestus.crypto.vrf.VRFMessage;
import io.Adrestus.util.SerializationUtil;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SerializeConsensusMessagesTest {
    @Test
    public void serialize_consensusVrfMessage() {
        BLSPrivateKey sk = new BLSPrivateKey(new SecureRandom());
        BLSPublicKey vk = new BLSPublicKey(sk);


        byte[] msg = "Test_Message".getBytes();
        Signature bls_sig = BLSSignature.sign(msg, sk);
        ////////////////////
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(BigDecimal.class, ctx -> new BigDecimalSerializer()));
        list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
        list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        list.add(new SerializationUtil.Mapping(TreeMap.class, ctx -> new CustomSerializerTreeMap()));
        Type fluentType = new TypeToken<ConsensusMessage<VRFMessage>>() {
        }.getType();
        SerializationUtil<ConsensusMessage> serialize = new SerializationUtil<ConsensusMessage>(fluentType, list);
        ////////////////////

        VRFMessage vrfMessage = new VRFMessage();
        vrfMessage.setType(VRFMessage.vrfMessageType.INIT);
        ConsensusMessage<VRFMessage> consensusMessage = new ConsensusMessage<>(vrfMessage);
        consensusMessage.setMessageType(ConsensusMessageType.COMMIT);
        ChecksumData checksumData = new ChecksumData();
        checksumData.setSignature(bls_sig);
        checksumData.setBlsPublicKey(vk);
        consensusMessage.setChecksumData(checksumData);
        BLSSignatureData blsSignatureData=new BLSSignatureData();
        blsSignatureData.getSignature()[0]=bls_sig;
        blsSignatureData.getMessageHash()[0]="hash";
        consensusMessage.getSignatures().put(vk,blsSignatureData);

        byte[] buffer = serialize.encode(consensusMessage);
        ConsensusMessage<VRFMessage> copy = serialize.decode(buffer);
        assertEquals(consensusMessage.getData(), vrfMessage);
        assertEquals(vrfMessage, copy.getData());
        assertEquals(consensusMessage, copy);
    }

    @Test
    public void serialize_consensusVDFMessage() {
        BLSPrivateKey sk = new BLSPrivateKey(new SecureRandom());
        BLSPublicKey vk = new BLSPublicKey(sk);


        byte[] msg = "Test_Message".getBytes();
        Signature bls_sig = BLSSignature.sign(msg, sk);
        ////////////////////
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(BigDecimal.class, ctx -> new BigDecimalSerializer()));
        list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
        list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        list.add(new SerializationUtil.Mapping(TreeMap.class, ctx -> new CustomSerializerTreeMap()));
        Type fluentType = new TypeToken<ConsensusMessage<VDFMessage>>() {
        }.getType();
        SerializationUtil<ConsensusMessage> serialize = new SerializationUtil<ConsensusMessage>(fluentType, list);


        VDFMessage vdfMessage=new VDFMessage();
        vdfMessage.setVDFSolution("asda".getBytes());
        ConsensusMessage<VDFMessage> consensusMessage = new ConsensusMessage<VDFMessage>(vdfMessage);
        consensusMessage.setMessageType(ConsensusMessageType.COMMIT);
        ChecksumData checksumData = new ChecksumData();
        checksumData.setSignature(bls_sig);
        checksumData.setBlsPublicKey(vk);
        consensusMessage.setChecksumData(checksumData);
        BLSSignatureData blsSignatureData=new BLSSignatureData();
        blsSignatureData.getSignature()[0]=bls_sig;
        blsSignatureData.getMessageHash()[0]="hash";
        consensusMessage.getSignatures().put(vk,blsSignatureData);

        byte[] buffer = serialize.encode(consensusMessage);
        ConsensusMessage<VDFMessage> copy = serialize.decode(buffer);
        assertEquals(consensusMessage.getData(), copy.getData());
        assertEquals(vdfMessage, copy.getData());
        assertEquals(consensusMessage, copy);


    }
}
