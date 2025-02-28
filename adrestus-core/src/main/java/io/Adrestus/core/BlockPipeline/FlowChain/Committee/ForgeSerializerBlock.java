package io.Adrestus.core.BlockPipeline.FlowChain.Committee;

import io.Adrestus.core.AbstractBlock;
import io.Adrestus.core.BlockPipeline.BlockRequest;
import io.Adrestus.core.BlockPipeline.BlockRequestHandler;
import io.Adrestus.core.BlockPipeline.BlockRequestType;
import io.Adrestus.core.CommitteeBlock;
import io.Adrestus.core.Util.BlockSizeCalculator;
import io.Adrestus.crypto.HashUtil;
import io.Adrestus.crypto.bls.BLS381.ECP;
import io.Adrestus.crypto.bls.BLS381.ECP2;
import io.Adrestus.crypto.bls.mapper.ECP2mapper;
import io.Adrestus.crypto.bls.mapper.ECPmapper;
import io.Adrestus.crypto.elliptic.mapper.BigDecimalSerializer;
import io.Adrestus.crypto.elliptic.mapper.BigIntegerSerializer;
import io.Adrestus.crypto.elliptic.mapper.CustomSerializerTreeMap;
import io.Adrestus.util.SerializationUtil;
import lombok.SneakyThrows;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class ForgeSerializerBlock implements BlockRequestHandler<CommitteeBlock> {

    private final BlockSizeCalculator blockSizeCalculator;
    private final SerializationUtil<AbstractBlock> encode;

    public ForgeSerializerBlock() {
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
        list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
        list.add(new SerializationUtil.Mapping(BigDecimal.class, ctx -> new BigDecimalSerializer()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        list.add(new SerializationUtil.Mapping(TreeMap.class, ctx -> new CustomSerializerTreeMap<>()));
        this.encode = new SerializationUtil<AbstractBlock>(AbstractBlock.class, list);
        blockSizeCalculator = new BlockSizeCalculator();
    }

    @Override
    public boolean canHandleRequest(BlockRequest<CommitteeBlock> req) {
        return req.getRequestType() == BlockRequestType.FORGE_SERIALIZER_BLOCK;
    }

    @Override
    public int getPriority() {
        return 5;
    }

    @Override
    @SneakyThrows
    public void process(BlockRequest<CommitteeBlock> blockRequest) {
        this.blockSizeCalculator.setCommitteeBlock(blockRequest.getBlock());
        String hash = HashUtil.sha256_bytetoString(encode.encode(blockRequest.getBlock(), this.blockSizeCalculator.CommitteeBlockSizeCalculator()));
        blockRequest.getBlock().setHash(hash);
    }

    @Override
    public String name() {
        return "ForgeSerializerBlock";
    }

    @Override
    public void clear(BlockRequest<CommitteeBlock> blockRequest) {
        blockRequest.clear();
    }
}
