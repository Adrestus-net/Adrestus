package io.Adrestus.core.BlockPipeline.FlowChain.Transaction;

import com.google.common.reflect.TypeToken;
import io.Adrestus.MemoryTreePool;
import io.Adrestus.config.SocketConfigOptions;
import io.Adrestus.core.BlockPipeline.BlockRequest;
import io.Adrestus.core.BlockPipeline.BlockRequestHandler;
import io.Adrestus.core.BlockPipeline.BlockRequestType;
import io.Adrestus.core.Resourses.CachedLatestBlocks;
import io.Adrestus.core.Resourses.CachedZoneIndex;
import io.Adrestus.core.Resourses.MemoryTransactionPool;
import io.Adrestus.core.Transaction;
import io.Adrestus.core.TransactionBlock;
import io.Adrestus.crypto.bls.BLS381.ECP;
import io.Adrestus.crypto.bls.BLS381.ECP2;
import io.Adrestus.crypto.bls.mapper.ECP2mapper;
import io.Adrestus.crypto.bls.mapper.ECPmapper;
import io.Adrestus.crypto.elliptic.mapper.BigDecimalSerializer;
import io.Adrestus.crypto.elliptic.mapper.BigIntegerSerializer;
import io.Adrestus.crypto.elliptic.mapper.CustomSerializerTreeMap;
import io.Adrestus.network.AsyncService;
import io.Adrestus.util.SerializationUtil;
import lombok.SneakyThrows;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class ForgeDifferentOriginBuilder implements BlockRequestHandler<TransactionBlock> {
    private final SerializationUtil<Transaction> transaction_encode;

    public ForgeDifferentOriginBuilder() {
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
        list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
        list.add(new SerializationUtil.Mapping(BigDecimal.class, ctx -> new BigDecimalSerializer()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        list.add(new SerializationUtil.Mapping(TreeMap.class, ctx -> new CustomSerializerTreeMap()));
        Type fluentType = new TypeToken<MemoryTreePool>() {
        }.getType();
        this.transaction_encode = new SerializationUtil<Transaction>(Transaction.class, list);
    }

    @Override
    public boolean canHandleRequest(BlockRequest<TransactionBlock> req) {
        return req.getRequestType() == BlockRequestType.FORGE_DIFFERENT_ORIGIN_BUILDER;
    }

    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    @SneakyThrows
    public void process(BlockRequest<TransactionBlock> blockRequest) {
        ArrayList<Transaction> todelete = new ArrayList<>(MemoryTransactionPool.getInstance().getListToDelete(CachedZoneIndex.getInstance().getZoneIndex()));
        todelete.stream().forEach(transaction -> {
            if (transaction.getZoneFrom() != CachedZoneIndex.getInstance().getZoneIndex()) {
                Thread.ofVirtual().start(() -> {
                    List<String> ips = CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(transaction.getZoneFrom()).values().stream().collect(Collectors.toList());
                    var executor = new AsyncService<Long>(ips, transaction_encode.encode(transaction, 1024), SocketConfigOptions.TRANSACTION_PORT);

                    var asyncResult1 = executor.startProcess(300L);
                    final var result1 = executor.endProcess(asyncResult1);
                    executor = null;
                });
            }
        });
        MemoryTransactionPool.getInstance().delete(todelete);
        todelete.clear();
    }

    @Override
    public void clear(BlockRequest<TransactionBlock> blockRequest) {
        blockRequest.clear();
    }

    @Override
    public String name() {
        return "ForgeDifferentOriginBuilder";
    }

}
