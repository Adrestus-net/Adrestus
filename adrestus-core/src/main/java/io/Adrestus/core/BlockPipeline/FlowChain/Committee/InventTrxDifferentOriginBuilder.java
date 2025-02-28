package io.Adrestus.core.BlockPipeline.FlowChain.Committee;

import io.Adrestus.config.SocketConfigOptions;
import io.Adrestus.core.BlockPipeline.BlockRequest;
import io.Adrestus.core.BlockPipeline.BlockRequestHandler;
import io.Adrestus.core.BlockPipeline.BlockRequestType;
import io.Adrestus.core.CommitteeBlock;
import io.Adrestus.core.Resourses.MemoryTransactionPool;
import io.Adrestus.core.Transaction;
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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class InventTrxDifferentOriginBuilder implements BlockRequestHandler<CommitteeBlock> {
    private final SerializationUtil<Transaction> transaction_encode;


    public InventTrxDifferentOriginBuilder() {
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
        list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
        list.add(new SerializationUtil.Mapping(BigDecimal.class, ctx -> new BigDecimalSerializer()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        list.add(new SerializationUtil.Mapping(TreeMap.class, ctx -> new CustomSerializerTreeMap()));
        this.transaction_encode = new SerializationUtil<Transaction>(Transaction.class, list);
    }

    @Override
    public boolean canHandleRequest(BlockRequest<CommitteeBlock> req) {
        return req.getRequestType() == BlockRequestType.INVENT_TRX_DIFFERENT_ORIGIN_BUILDER;
    }

    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    @SneakyThrows
    public void process(BlockRequest<CommitteeBlock> blockRequest) {
        //find transactions that is not for this zone and sent them to the correct zone
        List<Transaction> transactionList = MemoryTransactionPool.getInstance().getListByZone(blockRequest.getZoneIndex());
        List<byte[]> toSendTransaction = new ArrayList<>();
        transactionList.forEach(transaction -> toSendTransaction.add(transaction_encode.encode(transaction, 1024)));
        List<String> TransactionIPWorkers = new ArrayList<>(blockRequest.getBlock().getStructureMap().get(blockRequest.getZoneIndex()).values());

        if (!toSendTransaction.isEmpty()) {
            var executor = new AsyncService<Long>(TransactionIPWorkers, toSendTransaction, SocketConfigOptions.TRANSACTION_PORT);

            var asyncResult = executor.startListProcess(300L);
            var result = executor.endProcess(asyncResult);
            MemoryTransactionPool.getInstance().delete(transactionList);
        }
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
