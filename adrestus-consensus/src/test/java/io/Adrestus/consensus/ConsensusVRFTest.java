package io.Adrestus.consensus;

import com.google.common.reflect.TypeToken;
import io.Adrestus.core.Resourses.CachedBlocks;
import io.Adrestus.crypto.bls.model.BLSPrivateKey;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.bls.model.CachedBLSKeyPair;
import io.Adrestus.crypto.vrf.VRFMessage;
import io.Adrestus.util.SerializationUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.security.SecureRandom;

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

        CachedBlocks.getInstance().getCommitteeBlock().setHash("hash");
        CachedBlocks.getInstance().getCommitteeBlock().setViewID(10);


        BLSPrivateKey sk = new BLSPrivateKey(new SecureRandom());
        BLSPublicKey vk = new BLSPublicKey(sk);

        CachedBLSKeyPair.getInstance().setPrivateKey(sk);
        CachedBLSKeyPair.getInstance().setPublicKey(vk);

        ConsensusManager consensusManager = new ConsensusManager();
        consensusManager.changeStateTo(ConsensusRoleType.ORGANIZER);

        VRFConsensusPhase organizerphase = (VRFConsensusPhase) consensusManager.getRole().manufacturePhases(ConsensusType.VRF);
        VRFMessage organizer_message = new VRFMessage();
        organizerphase.Initialize(organizer_message);

        consensusManager.changeStateTo(ConsensusRoleType.VALIDATOR);
        VRFConsensusPhase validatorphase1 = (VRFConsensusPhase) consensusManager.getRole().manufacturePhases(ConsensusType.VRF);

        validatorphase1.Initialize(organizer_message);


        organizer_message.getSigners().add(organizer_message.getData());

        //Thread.sleep(1000);
        sk = new BLSPrivateKey(new SecureRandom());
        vk = new BLSPublicKey(sk);

        CachedBLSKeyPair.getInstance().setPrivateKey(sk);
        CachedBLSKeyPair.getInstance().setPublicKey(vk);

        validatorphase1.Initialize(organizer_message);


        organizer_message.getSigners().add(organizer_message.getData());

        organizerphase.AggregateVRF(organizer_message);

        ConsensusMessage<VRFMessage> consensusMessage = new ConsensusMessage<>(organizer_message);
        organizerphase.AnnouncePhase(consensusMessage);


        byte[] buffer = serialize.encode(consensusMessage);
        ConsensusMessage<VRFMessage> fg = serialize.decode(buffer);
        validatorphase1.AnnouncePhase(consensusMessage);
        consensusMessage.getSignatures().add(consensusMessage.getChecksumData());


        sk = new BLSPrivateKey(new SecureRandom());
        vk = new BLSPublicKey(sk);

        CachedBLSKeyPair.getInstance().setPrivateKey(sk);
        CachedBLSKeyPair.getInstance().setPublicKey(vk);

        validatorphase1.AnnouncePhase(consensusMessage);
        consensusMessage.getSignatures().add(consensusMessage.getChecksumData());


        organizerphase.PreparePhase(consensusMessage);


    }
}
