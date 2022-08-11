package io.Adrestus.consensus;

import io.Adrestus.core.CommitteeBlock;
import io.Adrestus.core.Resourses.CachedBlocks;
import io.Adrestus.core.TransactionBlock;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.bls.model.BLSSignature;
import io.Adrestus.crypto.bls.model.CachedBLSKeyPair;
import io.Adrestus.crypto.bls.model.Signature;
import io.Adrestus.crypto.vrf.VRFMessage;
import io.Adrestus.crypto.vrf.engine.VrfEngine2;
import io.Adrestus.util.ByteUtil;
import org.apache.tuweni.bytes.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ValidatorConsensusPhases {
    private static Logger LOG = LoggerFactory.getLogger(ValidatorConsensusPhases.class);

    public ValidatorConsensusPhases() {
    }

    protected static class VerifyVDF extends ValidatorConsensusPhases implements BFTConsensusPhase<String> {

        @Override
        public void AnnouncePhase(ConsensusMessage<String> data) {
            //data.setData("another data");
            System.out.println(data.toString());
        }

        @Override
        public void PreparePhase(ConsensusMessage<String> data) {

        }

        @Override
        public void CommitPhase(ConsensusMessage<String> data) {

        }
    }


    protected static class VerifyVRF extends ValidatorConsensusPhases implements VRFConsensusPhase<VRFMessage> {
        private VrfEngine2 group;
        private byte[] pRnd;

        public VerifyVRF() {
            this.group = new VrfEngine2();
        }

        @Override
        public void Initialize(VRFMessage message) throws Exception {
            if (!message.getType().equals(VRFMessage.vrfMessageType.INIT) ||
                    !message.getBlockHash().equals(CachedBlocks.getInstance().getCommitteeBlock().getHash()))
                throw new IllegalArgumentException("Organizer not produce valid vrf request");

            StringBuilder hashToVerify = new StringBuilder();


            hashToVerify.append(CachedBlocks.getInstance().getCommitteeBlock().getHash());
            hashToVerify.append(CachedBlocks.getInstance().getCommitteeBlock().getViewID());

            byte[] ri = group.prove(CachedBLSKeyPair.getInstance().getPrivateKey().toBytes(), hashToVerify.toString().getBytes(StandardCharsets.UTF_8));
            byte[] pi = group.proofToHash(ri);


            VRFMessage.VRFData data = new VRFMessage.VRFData(CachedBLSKeyPair.getInstance().getPublicKey().toBytes(), ri, pi);
            message.setData(data);
        }

        @Override
        public void AggregateVRF(VRFMessage message) throws Exception {

        }

        @Override
        public void AnnouncePhase(ConsensusMessage<VRFMessage> data) throws Exception {

            if (!data.getType().equals(ConsensusMessageType.ANNOUNCE))
                throw new IllegalArgumentException("Organizer not send correct header message");


            List<VRFMessage.VRFData> list = data.getData().getSigners();

            if (list.isEmpty())
                throw new IllegalArgumentException("Validators not produce valid vrf inputs and list is empty");

            StringBuilder hashToVerify = new StringBuilder();


            hashToVerify.append(CachedBlocks.getInstance().getCommitteeBlock().getHash());
            hashToVerify.append(CachedBlocks.getInstance().getCommitteeBlock().getViewID());


            for (int i = 0; i < list.size(); i++) {

                byte[] prove = group.verify(list.get(i).getBls_pubkey(), list.get(i).getRi(), hashToVerify.toString().getBytes(StandardCharsets.UTF_8));
                boolean retval = Arrays.equals(prove, list.get(i).getPi());

                if (!retval) {
                    LOG.info("VRF computation is not valid for this validator");
                    list.remove(i);
                }
            }


            byte[] res = list.get(0).getRi();
            for (int i = 0; i < list.size(); i++) {
                if (i == list.size() - 1) {
                    boolean retval = Arrays.equals(data.getData().getPrnd(), res);
                    if (!retval) {
                        throw new IllegalArgumentException("pRnd is not the same leader failure change view protocol");
                    }
                    this.pRnd = data.getData().getPrnd();
                    break;
                }
                res = ByteUtil.xor(res, list.get(i + 1).getRi());
            }
            Signature sig = BLSSignature.sign(this.pRnd, CachedBLSKeyPair.getInstance().getPrivateKey());
            data.setChecksumData(new ConsensusMessage.ChecksumData(sig, CachedBLSKeyPair.getInstance().getPublicKey()));
        }

        @Override
        public void PreparePhase(ConsensusMessage<VRFMessage> data) {
            if (!data.getType().equals(ConsensusMessageType.PREPARE))
                throw new IllegalArgumentException("Organizer not send correct header message");

            List<BLSPublicKey> publicKeys = data.getSignatures().stream().map(ConsensusMessage.ChecksumData::getBlsPublicKey).collect(Collectors.toList());
            List<Signature> signature = data.getSignatures().stream().map(ConsensusMessage.ChecksumData::getSignature).collect(Collectors.toList());


            Signature aggregatedSignature = BLSSignature.aggregate(signature);
            Bytes message = Bytes.wrap(data.getData().getPrnd());
            boolean verify = BLSSignature.fastAggregateVerify(publicKeys, message, aggregatedSignature);
            if (!verify)
                throw new IllegalArgumentException("Abort consensus phase BLS multi_signature is invalid");
        }

        @Override
        public void CommitPhase(ConsensusMessage<VRFMessage> data) {

        }


    }


    protected static class VerifyTransactionBlock extends ValidatorConsensusPhases implements BFTConsensusPhase<TransactionBlock> {


        @Override
        public void AnnouncePhase(ConsensusMessage<TransactionBlock> block) {
            System.out.println(block.toString());
        }

        @Override
        public void PreparePhase(ConsensusMessage<TransactionBlock> block) {

        }

        @Override
        public void CommitPhase(ConsensusMessage<TransactionBlock> block) {

        }
    }

    protected static class VerifyCommitteeBlock extends ValidatorConsensusPhases implements BFTConsensusPhase<CommitteeBlock> {

        @Override
        public void AnnouncePhase(ConsensusMessage<CommitteeBlock> block) {

        }

        @Override
        public void PreparePhase(ConsensusMessage<CommitteeBlock> block) {

        }

        @Override
        public void CommitPhase(ConsensusMessage<CommitteeBlock> block) {

        }
    }
}
