package io.Adrestus.consensus;

import io.Adrestus.core.Resourses.CachedLatestBlocks;
import io.Adrestus.core.Resourses.CachedLatestRandomness;
import io.Adrestus.crypto.bls.model.BLSPrivateKey;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.bls.model.CachedBLSKeyPair;
import io.Adrestus.crypto.vdf.VDFMessage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class ConsensusVDFTest {
    private static SecureRandom random;
    private static byte[] pRnd;

    @BeforeAll
    public static void setup() {
        pRnd = new byte[20];
        random = new SecureRandom();
        random.nextBytes(pRnd);
    }

    @Test
    public void ConsensusVDF() throws Exception {
        CachedLatestRandomness.getInstance().setpRnd(pRnd);
        CachedLatestBlocks.getInstance().getCommitteeBlock().setDifficulty(100);

        ConsensusManager consensusManager = new ConsensusManager();
        consensusManager.changeStateTo(ConsensusRoleType.ORGANIZER);

        BLSPrivateKey sk = new BLSPrivateKey(new SecureRandom());
        BLSPublicKey vk = new BLSPublicKey(sk);

        CachedBLSKeyPair.getInstance().setPrivateKey(sk);
        CachedBLSKeyPair.getInstance().setPublicKey(vk);

        BFTConsensusPhase organizerphase = (BFTConsensusPhase) consensusManager.getRole().manufacturePhases(ConsensusType.VDF);
        ConsensusMessage<VDFMessage> consensusMessage = new ConsensusMessage<>(new VDFMessage());
        organizerphase.AnnouncePhase(consensusMessage);


        consensusManager.changeStateTo(ConsensusRoleType.VALIDATOR);
        BFTConsensusPhase validatorphase = (BFTConsensusPhase) consensusManager.getRole().manufacturePhases(ConsensusType.VDF);

        validatorphase.AnnouncePhase(consensusMessage);
        if (consensusMessage.getStatusType().equals(ConsensusStatusType.SUCCESS))
            consensusMessage.getSignatures().add(consensusMessage.getChecksumData());

        sk = new BLSPrivateKey(new SecureRandom());
        vk = new BLSPublicKey(sk);

        CachedBLSKeyPair.getInstance().setPrivateKey(sk);
        CachedBLSKeyPair.getInstance().setPublicKey(vk);

        validatorphase.AnnouncePhase(consensusMessage);
        if (consensusMessage.getStatusType().equals(ConsensusStatusType.SUCCESS))
            consensusMessage.getSignatures().add(consensusMessage.getChecksumData());


        organizerphase.PreparePhase(consensusMessage);

        List<ConsensusMessage.ChecksumData> list = new ArrayList<>();
        validatorphase.PreparePhase(consensusMessage);
        if (consensusMessage.getStatusType().equals(ConsensusStatusType.SUCCESS))
            list.add(consensusMessage.getChecksumData());

        sk = new BLSPrivateKey(new SecureRandom());
        vk = new BLSPublicKey(sk);

        CachedBLSKeyPair.getInstance().setPrivateKey(sk);
        CachedBLSKeyPair.getInstance().setPublicKey(vk);

        validatorphase.PreparePhase(consensusMessage);
        if (consensusMessage.getStatusType().equals(ConsensusStatusType.SUCCESS))
            list.add(consensusMessage.getChecksumData());

        consensusMessage.clear();
        consensusMessage.setSignatures(list);

        organizerphase.CommitPhase(consensusMessage);

        validatorphase.CommitPhase(consensusMessage);
        validatorphase.CommitPhase(consensusMessage);
    }
}
