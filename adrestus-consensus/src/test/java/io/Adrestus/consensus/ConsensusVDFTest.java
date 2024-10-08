package io.Adrestus.consensus;

import io.Adrestus.core.Resourses.CachedLatestBlocks;
import io.Adrestus.core.Resourses.CachedSecurityHeaders;
import io.Adrestus.core.SortSignatureMapByBlsPublicKey;
import io.Adrestus.crypto.bls.BLSSignatureData;
import io.Adrestus.crypto.bls.model.BLSPrivateKey;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.bls.model.CachedBLSKeyPair;
import io.Adrestus.crypto.vdf.VDFMessage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.util.TreeMap;

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
        CachedSecurityHeaders.getInstance().getSecurityHeader().setPRnd(pRnd);
        CachedLatestBlocks.getInstance().getCommitteeBlock().setDifficulty(100);

        ConsensusManager consensusManager = new ConsensusManager(true);
        consensusManager.changeStateTo(ConsensusRoleType.SUPERVISOR);

        BLSPrivateKey leadersk = new BLSPrivateKey(new SecureRandom());
        BLSPublicKey leadervk = new BLSPublicKey(leadersk);

        BLSPrivateKey validator1sk = new BLSPrivateKey(new SecureRandom());
        BLSPublicKey validator1vk = new BLSPublicKey(validator1sk);

        BLSPrivateKey validator2sk = new BLSPrivateKey(new SecureRandom());
        BLSPublicKey validator2vk = new BLSPublicKey(validator2sk);

        CachedBLSKeyPair.getInstance().setPrivateKey(leadersk);
        CachedBLSKeyPair.getInstance().setPublicKey(leadervk);

        BFTConsensusPhase organizerphase = (BFTConsensusPhase) consensusManager.getRole().manufacturePhases(ConsensusType.VDF);
        ConsensusMessage<VDFMessage> consensusMessage = new ConsensusMessage<>(new VDFMessage());
        organizerphase.AnnouncePhase(consensusMessage);


        consensusManager.changeStateTo(ConsensusRoleType.VALIDATOR);
        BFTConsensusPhase validatorphase = (BFTConsensusPhase) consensusManager.getRole().manufacturePhases(ConsensusType.VDF);

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


        consensusMessage.clear();
        consensusMessage.setSignatures(list);
        CachedBLSKeyPair.getInstance().setPrivateKey(leadersk);
        CachedBLSKeyPair.getInstance().setPublicKey(leadervk);


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

        validatorphase.CommitPhase(consensusMessage);
        validatorphase.CommitPhase(consensusMessage);
    }
}
