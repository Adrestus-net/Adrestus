package io.Adrestus.consensus;

import io.Adrestus.config.AdrestusConfiguration;
import io.Adrestus.core.CommitteeBlock;
import io.Adrestus.core.Resourses.CachedLatestBlocks;
import io.Adrestus.core.Resourses.CachedLatestRandomness;
import io.Adrestus.core.TransactionBlock;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.bls.model.BLSSignature;
import io.Adrestus.crypto.bls.model.CachedBLSKeyPair;
import io.Adrestus.crypto.bls.model.Signature;
import io.Adrestus.crypto.vdf.VDFMessage;
import io.Adrestus.crypto.vdf.engine.VdfEngine;
import io.Adrestus.crypto.vdf.engine.VdfEnginePietrzak;
import io.Adrestus.crypto.vrf.VRFMessage;
import io.Adrestus.crypto.vrf.engine.VrfEngine2;
import io.Adrestus.util.ByteUtil;
import io.Adrestus.util.SerializationUtil;
import org.apache.tuweni.bytes.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ValidatorConsensusPhases {

    public ValidatorConsensusPhases() {
    }

    protected static class VerifyVDF extends ValidatorConsensusPhases implements BFTConsensusPhase<VDFMessage> {
        private static Logger LOG = LoggerFactory.getLogger(VerifyVDF.class);
        private final VdfEngine vdf;
        private final SerializationUtil<VDFMessage> serialize;
        public VerifyVDF() {
            vdf = new VdfEnginePietrzak(AdrestusConfiguration.PIERRZAK_BIT);
            serialize = new SerializationUtil<VDFMessage>(VDFMessage.class);
        }

        @Override
        public void AnnouncePhase(ConsensusMessage<VDFMessage> data) {
            if (!data.getMessageType().equals(ConsensusMessageType.ANNOUNCE))
                LOG.info("Organizer not send correct header message expected "+ConsensusMessageType.ANNOUNCE);

            boolean verify=vdf.verify(CachedLatestRandomness.getInstance().getpRnd(), CachedLatestBlocks.getInstance().getCommitteeBlock().getDifficulty(), data.getData().getVDFSolution());
            if (!verify) {
                LOG.info("Abort consensus phase VDF solution is invalid");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }
            CachedLatestRandomness.getInstance().setRnd(data.getData().getVDFSolution());
            data.setStatusType(ConsensusStatusType.SUCCESS);
            Signature sig = BLSSignature.sign(CachedLatestRandomness.getInstance().getRnd(), CachedBLSKeyPair.getInstance().getPrivateKey());
            data.setChecksumData(new ConsensusMessage.ChecksumData(sig, CachedBLSKeyPair.getInstance().getPublicKey()));
        }

        @Override
        public void PreparePhase(ConsensusMessage<VDFMessage> data) {
            if (!data.getMessageType().equals(ConsensusMessageType.PREPARE))
                LOG.info("Organizer not send correct header message expected "+ConsensusMessageType.PREPARE);

            List<BLSPublicKey> publicKeys = data.getSignatures().stream().map(ConsensusMessage.ChecksumData::getBlsPublicKey).collect(Collectors.toList());
            List<Signature> signature = data.getSignatures().stream().map(ConsensusMessage.ChecksumData::getSignature).collect(Collectors.toList());


            Signature aggregatedSignature = BLSSignature.aggregate(signature);
            Bytes toVerify = Bytes.wrap(CachedLatestRandomness.getInstance().getRnd());
            boolean verify = BLSSignature.fastAggregateVerify(publicKeys, toVerify, aggregatedSignature);
            if (!verify) {
                LOG.info("Abort consensus phase BLS multi_signature is invalid in prepare phase");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }

            // data.clear();
            data.setStatusType(ConsensusStatusType.SUCCESS);


            byte[] message = serialize.encode(data.getData());
            Signature sig = BLSSignature.sign(message, CachedBLSKeyPair.getInstance().getPrivateKey());
            data.setChecksumData(new ConsensusMessage.ChecksumData(sig, CachedBLSKeyPair.getInstance().getPublicKey()));
        }

        @Override
        public void CommitPhase(ConsensusMessage<VDFMessage> data) {
            if (!data.getMessageType().equals(ConsensusMessageType.COMMIT)) {
                LOG.info("Organizer not send correct header message expected "+ConsensusMessageType.COMMIT);
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }

            List<BLSPublicKey> publicKeys = data.getSignatures().stream().map(ConsensusMessage.ChecksumData::getBlsPublicKey).collect(Collectors.toList());
            List<Signature> signature = data.getSignatures().stream().map(ConsensusMessage.ChecksumData::getSignature).collect(Collectors.toList());


            Signature aggregatedSignature = BLSSignature.aggregate(signature);
            byte[] message = serialize.encode(data.getData());
            Bytes toVerify = Bytes.wrap(message);
            boolean verify = BLSSignature.fastAggregateVerify(publicKeys, toVerify, aggregatedSignature);
            if (!verify) {
                LOG.info("Abort consensus phase BLS multi_signature is invalid in prepare phase");
                data.setStatusType(ConsensusStatusType.ABORT);
            }

            //commit save to db
        }
    }


    protected static class VerifyVRF extends ValidatorConsensusPhases implements VRFConsensusPhase<VRFMessage> {
        private static Logger LOG = LoggerFactory.getLogger(VerifyVRF.class);
        private final VrfEngine2 group;
        private final SerializationUtil<VRFMessage> serialize;

        public VerifyVRF() {
            this.group = new VrfEngine2();
            serialize = new SerializationUtil<VRFMessage>(VRFMessage.class);
        }

        @Override
        public void Initialize(VRFMessage message) throws Exception {
            if (!message.getType().equals(VRFMessage.vrfMessageType.INIT) ||
                    !message.getBlockHash().equals(CachedLatestBlocks.getInstance().getCommitteeBlock().getHash()))
                throw new IllegalArgumentException("Organizer not produce valid vrf request");

            StringBuilder hashToVerify = new StringBuilder();


            hashToVerify.append(CachedLatestBlocks.getInstance().getCommitteeBlock().getHash());
            hashToVerify.append(CachedLatestBlocks.getInstance().getCommitteeBlock().getViewID());

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

            if (!data.getMessageType().equals(ConsensusMessageType.ANNOUNCE))
                throw new IllegalArgumentException("Organizer not send correct header message expected "+ConsensusMessageType.ANNOUNCE);


            List<VRFMessage.VRFData> list = data.getData().getSigners();

            if (list.isEmpty())
                throw new IllegalArgumentException("Validators not produce valid vrf inputs and list is empty");

            StringBuilder hashToVerify = new StringBuilder();


            hashToVerify.append(CachedLatestBlocks.getInstance().getCommitteeBlock().getHash());
            hashToVerify.append(CachedLatestBlocks.getInstance().getCommitteeBlock().getViewID());


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
                    CachedLatestRandomness.getInstance().setpRnd(data.getData().getPrnd());
                    break;
                }
                res = ByteUtil.xor(res, list.get(i + 1).getRi());
            }
            Signature sig = BLSSignature.sign(CachedLatestRandomness.getInstance().getpRnd(), CachedBLSKeyPair.getInstance().getPrivateKey());
            data.setChecksumData(new ConsensusMessage.ChecksumData(sig, CachedBLSKeyPair.getInstance().getPublicKey()));
        }

        @Override
        public void PreparePhase(ConsensusMessage<VRFMessage> data) {
            if (!data.getMessageType().equals(ConsensusMessageType.PREPARE)) {
                LOG.info("Organizer not send correct header message expected "+ConsensusMessageType.PREPARE);
                //  data.clear();
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }
            List<BLSPublicKey> publicKeys = data.getSignatures().stream().map(ConsensusMessage.ChecksumData::getBlsPublicKey).collect(Collectors.toList());
            List<Signature> signature = data.getSignatures().stream().map(ConsensusMessage.ChecksumData::getSignature).collect(Collectors.toList());


            Signature aggregatedSignature = BLSSignature.aggregate(signature);
            Bytes toVerify = Bytes.wrap(data.getData().getPrnd());
            boolean verify = BLSSignature.fastAggregateVerify(publicKeys, toVerify, aggregatedSignature);
            if (!verify) {
                LOG.info("Abort consensus phase BLS multi_signature is invalid in prepare phase");
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }

            // data.clear();
            data.setStatusType(ConsensusStatusType.SUCCESS);


            byte[] message = serialize.encode(data.getData());
            Signature sig = BLSSignature.sign(message, CachedBLSKeyPair.getInstance().getPrivateKey());
            data.setChecksumData(new ConsensusMessage.ChecksumData(sig, CachedBLSKeyPair.getInstance().getPublicKey()));
        }

        @Override
        public void CommitPhase(ConsensusMessage<VRFMessage> data) {
            if (!data.getMessageType().equals(ConsensusMessageType.COMMIT)) {
                LOG.info("Organizer not send correct header message expected "+ConsensusMessageType.COMMIT);
                data.setStatusType(ConsensusStatusType.ABORT);
                return;
            }

            List<BLSPublicKey> publicKeys = data.getSignatures().stream().map(ConsensusMessage.ChecksumData::getBlsPublicKey).collect(Collectors.toList());
            List<Signature> signature = data.getSignatures().stream().map(ConsensusMessage.ChecksumData::getSignature).collect(Collectors.toList());


            Signature aggregatedSignature = BLSSignature.aggregate(signature);
            byte[] message = serialize.encode(data.getData());
            Bytes toVerify = Bytes.wrap(message);
            boolean verify = BLSSignature.fastAggregateVerify(publicKeys, toVerify, aggregatedSignature);
            if (!verify) {
                LOG.info("Abort consensus phase BLS multi_signature is invalid in prepare phase");
                data.setStatusType(ConsensusStatusType.ABORT);
            }

            //commit save to db
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
