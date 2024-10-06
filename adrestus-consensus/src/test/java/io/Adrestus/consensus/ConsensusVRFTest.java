package io.Adrestus.consensus;

import com.google.common.reflect.TypeToken;
import io.Adrestus.core.Resourses.CachedLatestBlocks;
import io.Adrestus.crypto.bls.BLSSignatureData;
import io.Adrestus.crypto.bls.model.BLSPrivateKey;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.bls.model.CachedBLSKeyPair;
import io.Adrestus.crypto.vrf.VRFMessage;
import io.Adrestus.util.SerializationUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.security.SecureRandom;
import java.util.HashMap;

public class ConsensusVRFTest {
    private static SerializationUtil<ConsensusMessage> serialize;

    @BeforeAll
    public static void setup() {
        Type fluentType = new TypeToken<ConsensusMessage<VRFMessage>>() {
        }.getType();
        serialize = new SerializationUtil<ConsensusMessage>(fluentType);
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


        HashMap<BLSPublicKey, BLSSignatureData> list = new HashMap<>();

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


        HashMap<BLSPublicKey, BLSSignatureData> list1 = new HashMap<>();

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
