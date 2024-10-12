package io.Adrestus.consensus;

import com.google.common.reflect.TypeToken;
import io.Adrestus.core.Resourses.CachedLatestBlocks;
import io.Adrestus.core.comparators.SortSignatureMapByBlsPublicKey;
import io.Adrestus.core.mapper.CustomSerializerTreeMap;
import io.Adrestus.crypto.bls.BLS381.ECP;
import io.Adrestus.crypto.bls.BLS381.ECP2;
import io.Adrestus.crypto.bls.BLSSignatureData;
import io.Adrestus.crypto.bls.mapper.ECP2mapper;
import io.Adrestus.crypto.bls.mapper.ECPmapper;
import io.Adrestus.crypto.bls.model.BLSPrivateKey;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.bls.model.CachedBLSKeyPair;
import io.Adrestus.crypto.elliptic.mapper.BigDecimalSerializer;
import io.Adrestus.crypto.elliptic.mapper.BigIntegerSerializer;
import io.Adrestus.crypto.vrf.VRFMessage;
import io.Adrestus.util.SerializationUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class ConsensusVRFTest {
    private static SerializationUtil<ConsensusMessage> serialize;

    @BeforeAll
    public static void setup() {
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
        list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
        list.add(new SerializationUtil.Mapping(BigDecimal.class, ctx -> new BigDecimalSerializer()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        list.add(new SerializationUtil.Mapping(TreeMap.class, ctx -> new CustomSerializerTreeMap()));
        Type fluentType = new TypeToken<ConsensusMessage<VRFMessage>>() {
        }.getType();
        serialize = new SerializationUtil<ConsensusMessage>(fluentType, list);
    }

    @Test
    public void ConsensusVRF() throws Exception {

        CachedLatestBlocks.getInstance().getCommitteeBlock().setHash("hash");
        CachedLatestBlocks.getInstance().getCommitteeBlock().setViewID(10);


        BLSPrivateKey leadersk = new BLSPrivateKey(new SecureRandom());
        BLSPublicKey leadervk = new BLSPublicKey(leadersk);

        BLSPrivateKey validator1sk = new BLSPrivateKey(new SecureRandom());
        BLSPublicKey validator1vk = new BLSPublicKey(validator1sk);

        BLSPrivateKey validator2sk = new BLSPrivateKey(new SecureRandom());
        BLSPublicKey validator2vk = new BLSPublicKey(validator2sk);

        CachedBLSKeyPair.getInstance().setPrivateKey(leadersk);
        CachedBLSKeyPair.getInstance().setPublicKey(leadervk);

        ConsensusManager consensusManager = new ConsensusManager(true);
        consensusManager.changeStateTo(ConsensusRoleType.SUPERVISOR);

        VRFConsensusPhase organizerphase = (VRFConsensusPhase) consensusManager.getRole().manufacturePhases(ConsensusType.VRF);
        VRFMessage organizer_message = new VRFMessage();
        organizerphase.Initialize(organizer_message);

        consensusManager.changeStateTo(ConsensusRoleType.VALIDATOR);
        VRFConsensusPhase validatorphase = (VRFConsensusPhase) consensusManager.getRole().manufacturePhases(ConsensusType.VRF);

        validatorphase.Initialize(organizer_message);


        organizer_message.getSigners().add(organizer_message.getData());


        CachedBLSKeyPair.getInstance().setPrivateKey(validator1sk);
        CachedBLSKeyPair.getInstance().setPublicKey(validator1vk);

        validatorphase.Initialize(organizer_message);


        CachedBLSKeyPair.getInstance().setPrivateKey(leadersk);
        CachedBLSKeyPair.getInstance().setPublicKey(leadervk);

        organizer_message.getSigners().add(organizer_message.getData());

        organizerphase.AggregateVRF(organizer_message);

        ConsensusMessage<VRFMessage> consensusMessage = new ConsensusMessage<>(organizer_message);
        organizerphase.AnnouncePhase(consensusMessage);


        TreeMap<BLSPublicKey, BLSSignatureData> list = new TreeMap<BLSPublicKey, BLSSignatureData>(new SortSignatureMapByBlsPublicKey());

        CachedBLSKeyPair.getInstance().setPrivateKey(validator1sk);
        CachedBLSKeyPair.getInstance().setPublicKey(validator1vk);

        validatorphase.AnnouncePhase(consensusMessage);
        if (consensusMessage.getStatusType().equals(ConsensusStatusType.SUCCESS)) {
            BLSSignatureData blsSignatureData = new BLSSignatureData();
            blsSignatureData.getSignature()[0] = consensusMessage.getChecksumData().getSignature();
            list.put(consensusMessage.getChecksumData().getBlsPublicKey(), blsSignatureData);
        }

        CachedBLSKeyPair.getInstance().setPrivateKey(validator2sk);
        CachedBLSKeyPair.getInstance().setPublicKey(validator2vk);

        validatorphase.AnnouncePhase(consensusMessage);
        if (consensusMessage.getStatusType().equals(ConsensusStatusType.SUCCESS)) {
            BLSSignatureData blsSignatureData = new BLSSignatureData();
            blsSignatureData.getSignature()[0] = consensusMessage.getChecksumData().getSignature();
            list.put(consensusMessage.getChecksumData().getBlsPublicKey(), blsSignatureData);
        }

        byte[] buffer = serialize.encode(consensusMessage);
        ConsensusMessage<VRFMessage> fg = serialize.decode(buffer);

        consensusMessage.clear();
        consensusMessage.setSignatures(list);

        organizerphase.PreparePhase(consensusMessage);


        TreeMap<BLSPublicKey, BLSSignatureData> list1 = new TreeMap<BLSPublicKey, BLSSignatureData>(new SortSignatureMapByBlsPublicKey());

        CachedBLSKeyPair.getInstance().setPrivateKey(validator1sk);
        CachedBLSKeyPair.getInstance().setPublicKey(validator1vk);

        validatorphase.PreparePhase(consensusMessage);
        if (consensusMessage.getStatusType().equals(ConsensusStatusType.SUCCESS)) {
            BLSSignatureData blsSignatureData = new BLSSignatureData();
            blsSignatureData.getSignature()[1] = consensusMessage.getChecksumData().getSignature();
            list1.put(consensusMessage.getChecksumData().getBlsPublicKey(), blsSignatureData);
        }

        CachedBLSKeyPair.getInstance().setPrivateKey(validator2sk);
        CachedBLSKeyPair.getInstance().setPublicKey(validator2vk);

        validatorphase.PreparePhase(consensusMessage);
        if (consensusMessage.getStatusType().equals(ConsensusStatusType.SUCCESS)) {
            BLSSignatureData blsSignatureData = new BLSSignatureData();
            blsSignatureData.getSignature()[1] = consensusMessage.getChecksumData().getSignature();
            list1.put(consensusMessage.getChecksumData().getBlsPublicKey(), blsSignatureData);
        }


        CachedBLSKeyPair.getInstance().setPrivateKey(leadersk);
        CachedBLSKeyPair.getInstance().setPublicKey(leadervk);

        consensusMessage.clear();
        consensusMessage.setSignatures(list1);

        organizerphase.CommitPhase(consensusMessage);

        CachedBLSKeyPair.getInstance().setPrivateKey(validator1sk);
        CachedBLSKeyPair.getInstance().setPublicKey(validator1vk);

        validatorphase.CommitPhase(consensusMessage);

        CachedBLSKeyPair.getInstance().setPrivateKey(validator2sk);
        CachedBLSKeyPair.getInstance().setPublicKey(validator2vk);

        validatorphase.CommitPhase(consensusMessage);

    }
}
