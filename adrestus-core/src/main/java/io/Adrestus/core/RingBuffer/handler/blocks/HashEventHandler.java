package io.Adrestus.core.RingBuffer.handler.blocks;

import io.Adrestus.core.AbstractBlock;
import io.Adrestus.core.CommitteeBlock;
import io.Adrestus.core.RingBuffer.event.AbstractBlockEvent;
import io.Adrestus.core.StatusType;
import io.Adrestus.core.TransactionBlock;
import io.Adrestus.crypto.HashUtil;
import io.Adrestus.crypto.bls.BLS381.ECP;
import io.Adrestus.crypto.bls.BLS381.ECP2;
import io.Adrestus.crypto.bls.mapper.ECP2mapper;
import io.Adrestus.crypto.bls.mapper.ECPmapper;
import io.Adrestus.crypto.elliptic.mapper.BigIntegerSerializer;
import io.Adrestus.crypto.elliptic.mapper.CustomSerializerTreeMap;
import io.Adrestus.util.SerializationUtil;
import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class HashEventHandler implements BlockEventHandler<AbstractBlockEvent>, DisruptorBlockVisitor {

    private static Logger LOG = LoggerFactory.getLogger(HashEventHandler.class);
    private final SerializationUtil<AbstractBlock> wrapper;

    public HashEventHandler() {
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
        list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        list.add(new SerializationUtil.Mapping(TreeMap.class, ctx -> new CustomSerializerTreeMap()));
        wrapper = new SerializationUtil<AbstractBlock>(AbstractBlock.class, list);
    }

    @Override
    public void onEvent(AbstractBlockEvent blockEvent, long l, boolean b) throws Exception {
        try {
            AbstractBlock block = blockEvent.getBlock();
            block.accept(this);
        } catch (NullPointerException ex) {
            LOG.info("Block is empty");
        }
    }

    @Override
    public void visit(CommitteeBlock committeeBlock) {
        try {
            if (committeeBlock.getHash().length() != 64) {
                LOG.info("Block hashes length is not valid");
                committeeBlock.setStatustype(StatusType.ABORT);
            }
            CommitteeBlock cloneable = (CommitteeBlock) committeeBlock.clone();
            int length=SerializationUtils.serialize(cloneable).length;
            cloneable.setHash("");
            byte[] buffer = wrapper.encodeNotOptimal(cloneable, length);
            String result_hash = HashUtil.sha256_bytetoString(buffer);
            if (!result_hash.equals(committeeBlock.getHash())) {
                byte[] buffer_prev = wrapper.encodeNotOptimalPrevious(cloneable, length);
                String result_hash_prev = HashUtil.sha256_bytetoString(buffer_prev);
                if (!result_hash_prev.equals(committeeBlock.getHash())) {
                    LOG.info("Block hash is manipulated");
                    committeeBlock.setStatustype(StatusType.ABORT);
                }
            }
        } catch (NullPointerException ex) {
            LOG.info("Block is empty");
            committeeBlock.setStatustype(StatusType.ABORT);
        } catch (CloneNotSupportedException e) {
            LOG.info("Block clone error ");
            committeeBlock.setStatustype(StatusType.ABORT);
        }
    }

    @Override
    public void visit(TransactionBlock transactionBlock) {
        try {
            if (transactionBlock.getHash().length() != 64) {
                LOG.info("Block hashes length is not valid");
                transactionBlock.setStatustype(StatusType.ABORT);
            }
            TransactionBlock cloneable = (TransactionBlock) transactionBlock.clone();
            cloneable.setHash("");
            byte[] buffer = wrapper.encode(cloneable);
            String result_hash = HashUtil.sha256_bytetoString(buffer);
            if (!result_hash.equals(transactionBlock.getHash())) {
                LOG.info("Block hash is manipulated");
                transactionBlock.setStatustype(StatusType.ABORT);
            }
        } catch (NullPointerException ex) {
            LOG.info("Block is empty");
            transactionBlock.setStatustype(StatusType.ABORT);
        } catch (CloneNotSupportedException e) {
            LOG.info("Block clone error ");
            transactionBlock.setStatustype(StatusType.ABORT);
        }
    }

}
