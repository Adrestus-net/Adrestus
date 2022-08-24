package io.Adrestus.consensus;

import io.Adrestus.config.AdrestusConfiguration;
import io.Adrestus.core.*;
import io.Adrestus.core.Resourses.CachedLatestBlocks;
import io.Adrestus.core.Resourses.CachedLatestRandomness;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.bls.model.BLSSignature;
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

public class OrganizerConsensusPhases {

    protected static class ProposeVDF extends OrganizerConsensusPhases implements BFTConsensusPhase<VDFMessage> {
        private static Logger LOG = LoggerFactory.getLogger(ProposeVDF.class);
        private final VdfEngine vdf;
        private final SerializationUtil<VDFMessage> serialize;

        public ProposeVDF() {
            vdf = new VdfEnginePietrzak(AdrestusConfiguration.PIERRZAK_BIT);
            this.serialize = new SerializationUtil<VDFMessage>(VDFMessage.class);
        }

        @Override
        public void AnnouncePhase(ConsensusMessage<VDFMessage> data) {
            data.setMessageType(ConsensusMessageType.ANNOUNCE);
            byte[] solution = vdf.solve(CachedLatestRandomness.getInstance().getpRnd(), CachedLatestBlocks.getInstance().getCommitteeBlock().getDifficulty());
            data.getData().setVDFSolution(solution);
        }

        @Override
        public void PreparePhase(ConsensusMessage<VDFMessage> data) {
            data.setMessageType(ConsensusMessageType.PREPARE);

            List<BLSPublicKey> publicKeys = data.getSignatures().stream().map(ConsensusMessage.ChecksumData::getBlsPublicKey).collect(Collectors.toList());
            List<Signature> signature = data.getSignatures().stream().map(ConsensusMessage.ChecksumData::getSignature).collect(Collectors.toList());


            Signature aggregatedSignature = BLSSignature.aggregate(signature);
            Bytes message = Bytes.wrap(CachedLatestRandomness.getInstance().getRnd());
            boolean verify = BLSSignature.fastAggregateVerify(publicKeys, message, aggregatedSignature);
            if (!verify)
                throw new IllegalArgumentException("Abort consensus phase BLS multi_signature is invalid during prepare phase");
        }

        @Override
        public void CommitPhase(ConsensusMessage<VDFMessage> data) {
            data.setMessageType(ConsensusMessageType.COMMIT);

            List<BLSPublicKey> publicKeys = data.getSignatures().stream().map(ConsensusMessage.ChecksumData::getBlsPublicKey).collect(Collectors.toList());
            List<Signature> signature = data.getSignatures().stream().map(ConsensusMessage.ChecksumData::getSignature).collect(Collectors.toList());


            Signature aggregatedSignature = BLSSignature.aggregate(signature);
            byte[] wrapp = serialize.encode(data.getData());
            Bytes message = Bytes.wrap(wrapp);
            boolean verify = BLSSignature.fastAggregateVerify(publicKeys, message, aggregatedSignature);
            if (!verify)
                throw new IllegalArgumentException("Abort consensus phase BLS multi_signature is invalid during commit phase");

            //commit save to db
        }
    }


    protected static class ProposeVRF extends OrganizerConsensusPhases implements VRFConsensusPhase<VRFMessage> {
        private static Logger LOG = LoggerFactory.getLogger(ProposeVRF.class);
        private VrfEngine2 group;
        private final SerializationUtil<VRFMessage> serialize;

        public ProposeVRF() {
            this.group = new VrfEngine2();
            this.serialize = new SerializationUtil<VRFMessage>(VRFMessage.class);
        }

        @Override
        public void Initialize(VRFMessage message) {
            message.setBlockHash(CachedLatestBlocks.getInstance().getCommitteeBlock().getHash());
            message.setType(VRFMessage.vrfMessageType.INIT);
        }


        public void AggregateVRF(VRFMessage message) throws Exception {
            List<VRFMessage.VRFData> list = message.getSigners();

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
                    message.setPrnd(res);
                    break;
                }
                res = ByteUtil.xor(res, list.get(i + 1).getRi());
            }
        }

        @Override
        public void AnnouncePhase(ConsensusMessage<VRFMessage> data) {
            data.setMessageType(ConsensusMessageType.ANNOUNCE);
        }

        @Override
        public void PreparePhase(ConsensusMessage<VRFMessage> data) {
            data.setMessageType(ConsensusMessageType.PREPARE);

            List<BLSPublicKey> publicKeys = data.getSignatures().stream().map(ConsensusMessage.ChecksumData::getBlsPublicKey).collect(Collectors.toList());
            List<Signature> signature = data.getSignatures().stream().map(ConsensusMessage.ChecksumData::getSignature).collect(Collectors.toList());


            Signature aggregatedSignature = BLSSignature.aggregate(signature);
            Bytes message = Bytes.wrap(data.getData().getPrnd());
            boolean verify = BLSSignature.fastAggregateVerify(publicKeys, message, aggregatedSignature);
            if (!verify)
                throw new IllegalArgumentException("Abort consensus phase BLS multi_signature is invalid during prepare phase");
        }

        @Override
        public void CommitPhase(ConsensusMessage<VRFMessage> data) {
            data.setMessageType(ConsensusMessageType.COMMIT);

            List<BLSPublicKey> publicKeys = data.getSignatures().stream().map(ConsensusMessage.ChecksumData::getBlsPublicKey).collect(Collectors.toList());
            List<Signature> signature = data.getSignatures().stream().map(ConsensusMessage.ChecksumData::getSignature).collect(Collectors.toList());


            Signature aggregatedSignature = BLSSignature.aggregate(signature);
            byte[] wrapp = serialize.encode(data.getData());
            Bytes message = Bytes.wrap(wrapp);
            boolean verify = BLSSignature.fastAggregateVerify(publicKeys, message, aggregatedSignature);
            if (!verify)
                throw new IllegalArgumentException("Abort consensus phase BLS multi_signature is invalid during commit phase");

            //commit save to db
        }


    }

    protected static class ProposeCommitteeBlock extends OrganizerConsensusPhases implements BFTConsensusPhase<CommitteeBlock> {

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
