package io.Adrestus.core.BlockPipeline.FlowChain.Transaction;

import com.google.common.reflect.TypeToken;
import io.Adrestus.MemoryTreePool;
import io.Adrestus.config.SocketConfigOptions;
import io.Adrestus.core.BlockPipeline.BlockRequest;
import io.Adrestus.core.BlockPipeline.BlockRequestHandler;
import io.Adrestus.core.BlockPipeline.BlockRequestType;
import io.Adrestus.core.Receipt;
import io.Adrestus.core.Resourses.CachedLatestBlocks;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

public class InventOutBoundBuilder implements BlockRequestHandler<TransactionBlock> {
    private static final Logger log = LoggerFactory.getLogger(InventInBoundBuilder.class);
    private final SerializationUtil<Receipt> receipt_encode;

    public InventOutBoundBuilder() {
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
        list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
        list.add(new SerializationUtil.Mapping(BigDecimal.class, ctx -> new BigDecimalSerializer()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        list.add(new SerializationUtil.Mapping(TreeMap.class, ctx -> new CustomSerializerTreeMap()));
        Type fluentType = new TypeToken<MemoryTreePool>() {
        }.getType();
        this.receipt_encode = new SerializationUtil<Receipt>(Receipt.class, list);
    }

    @Override
    public boolean canHandleRequest(BlockRequest<TransactionBlock> req) {
        return req.getRequestType() == BlockRequestType.INVENT_OUTBOUND_BUILDER;
    }

    @Override
    public int getPriority() {
        return 3;
    }

    @Override
    @SneakyThrows
    public void process(BlockRequest<TransactionBlock> blockRequest) {
        if (!blockRequest.getBlock().getOutbound().getMap_receipts().isEmpty()) {
            Thread.ofVirtual().start(() -> {
                for (Map.Entry<Integer, LinkedHashMap<Receipt.ReceiptBlock, List<Receipt>>> entry : blockRequest.getBlock().getOutbound().getMap_receipts().entrySet()) {
                    List<String> ReceiptIPWorkers = CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(entry.getKey()).values().stream().collect(Collectors.toList());
                    List<byte[]> toSendReceipt = new ArrayList<>();
                    for (Map.Entry<Receipt.ReceiptBlock, List<Receipt>> entry2 : entry.getValue().entrySet()) {
                        entry2.getValue().forEach(receipt -> toSendReceipt.add(receipt_encode.encode(receipt, 1024)));
                    }
                    var executor = new AsyncService<Long>(ReceiptIPWorkers, toSendReceipt, SocketConfigOptions.RECEIPT_PORT);
                    var asyncResult = executor.startListProcess(300L);
                    var result = executor.endProcess(asyncResult);
                }
            });
        }

    }

    @Override
    public void clear(BlockRequest<TransactionBlock> blockRequest) {
        blockRequest.clear();
    }

    @Override
    public String name() {
        return "InventOutBoundBuilder";
    }
}
