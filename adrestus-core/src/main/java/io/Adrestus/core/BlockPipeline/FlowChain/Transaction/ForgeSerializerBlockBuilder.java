package io.Adrestus.core.BlockPipeline.FlowChain.Transaction;

import io.Adrestus.core.AbstractBlock;
import io.Adrestus.core.BlockPipeline.BlockRequest;
import io.Adrestus.core.BlockPipeline.BlockRequestHandler;
import io.Adrestus.core.BlockPipeline.BlockRequestType;
import io.Adrestus.core.TransactionBlock;
import io.Adrestus.core.Util.BlockSizeCalculator;
import io.Adrestus.crypto.HashUtil;
import io.Adrestus.crypto.bls.BLS381.ECP;
import io.Adrestus.crypto.bls.BLS381.ECP2;
import io.Adrestus.crypto.bls.BLSSignatureData;
import io.Adrestus.crypto.bls.mapper.ECP2mapper;
import io.Adrestus.crypto.bls.mapper.ECPmapper;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
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

public class ForgeSerializerBlockBuilder implements BlockRequestHandler<TransactionBlock> {
    private final BlockSizeCalculator blockSizeCalculator;
    private final SerializationUtil<AbstractBlock> encode;

    public ForgeSerializerBlockBuilder() {
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
        list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
        list.add(new SerializationUtil.Mapping(BigDecimal.class, ctx -> new BigDecimalSerializer()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        list.add(new SerializationUtil.Mapping(TreeMap.class, ctx -> new CustomSerializerTreeMap<BLSPublicKey, BLSSignatureData>()));
        this.encode = new SerializationUtil<AbstractBlock>(AbstractBlock.class, list);
        this.blockSizeCalculator = new BlockSizeCalculator();
    }

    @Override
    public boolean canHandleRequest(BlockRequest<TransactionBlock> req) {
        return req.getRequestType() == BlockRequestType.FORGE_SERIALIZER_BLOCK_BUILDER;
    }

    @Override
    public int getPriority() {
        return 6;
    }

    @Override
    @SneakyThrows
    public void process(BlockRequest<TransactionBlock> blockRequest) {
        this.blockSizeCalculator.setTransactionBlock(blockRequest.getBlock());
        byte[] tohash = encode.encode(blockRequest.getBlock(), this.blockSizeCalculator.TransactionBlockSizeCalculator());
        blockRequest.getBlock().setHash(HashUtil.sha256_bytetoString(tohash));
    }

    @Override
    public void clear(BlockRequest<TransactionBlock> blockRequest) {
        blockRequest.clear();
    }

    @Override
    public String name() {
        return "ForgeSerializerBlockBuilder";
    }
}
