package io.Adrestus.core.BlockPipeline.FlowChain.Transaction;

import com.google.common.reflect.TypeToken;
import io.Adrestus.MemoryTreePool;
import io.Adrestus.config.SocketConfigOptions;
import io.Adrestus.core.BlockPipeline.BlockRequest;
import io.Adrestus.core.BlockPipeline.BlockRequestHandler;
import io.Adrestus.core.BlockPipeline.BlockRequestType;
import io.Adrestus.core.InboundRelay;
import io.Adrestus.core.Receipt;
import io.Adrestus.core.Resourses.CachedInboundTransactionBlocks;
import io.Adrestus.core.Resourses.CachedLatestBlocks;
import io.Adrestus.core.Resourses.CachedZoneIndex;
import io.Adrestus.core.Resourses.MemoryReceiptPool;
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
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class ForgeInBoundBuilder implements BlockRequestHandler<TransactionBlock> {
    private final SerializationUtil<Receipt> receipt_encode;

    public ForgeInBoundBuilder() {
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
        return req.getRequestType() == BlockRequestType.FORGE_INBOUND_BUILDER;
    }

    @Override
    public int getPriority() {
        return 4;
    }

    @Override
    @SneakyThrows
    public void process(BlockRequest<TransactionBlock> blockRequest) {
        if (!MemoryReceiptPool.getInstance().getAll().isEmpty()) {
            List<Receipt> receiptList1 = MemoryReceiptPool.getInstance().getOutBoundList(CachedZoneIndex.getInstance().getZoneIndex());
            if (!receiptList1.isEmpty()) {
                Map<Integer, List<Receipt>> receiptListGrouped = receiptList1.stream().collect(Collectors.groupingBy(w -> w.getZoneTo()));
                for (Map.Entry<Integer, List<Receipt>> entry : receiptListGrouped.entrySet()) {
                    List<String> ReceiptIPWorkers = CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(entry.getKey()).values().stream().collect(Collectors.toList());
                    List<byte[]> toSendReceipt = new ArrayList<>();
                    entry.getValue().stream().forEach(receipt -> toSendReceipt.add(receipt_encode.encode(receipt, 1024)));

                    if (!toSendReceipt.isEmpty()) {
                        Thread.ofVirtual().start(() -> {
                            var executor = new AsyncService<Long>(ReceiptIPWorkers, toSendReceipt, SocketConfigOptions.RECEIPT_PORT);

                            var asyncResult = executor.startListProcess(300L);
                            var result = executor.endProcess(asyncResult);
                            MemoryReceiptPool.getInstance().delete(entry.getValue());
                        });
                    }
                }
            }
            Map<Integer, Map<Receipt.ReceiptBlock, List<Receipt>>> inbound_map = (new ArrayList<Receipt>((ArrayList<Receipt>) MemoryReceiptPool.getInstance().getInboundList(CachedZoneIndex.getInstance().getZoneIndex())))
                    .stream()
                    .collect(Collectors.groupingBy(Receipt::getZoneFrom, Collectors.groupingBy(Receipt::getReceiptBlock)));
            InboundRelay inboundRelay = new InboundRelay(inbound_map);
            blockRequest.getBlock().setInbound(inboundRelay);
            CachedInboundTransactionBlocks.getInstance().generate(inboundRelay.getMap_receipts(), blockRequest.getBlock().getGeneration());
        }
    }

    @Override
    public void clear(BlockRequest<TransactionBlock> blockRequest) {
        blockRequest.clear();
    }

    @Override
    public String name() {
        return "ForgeInBoundBuilder";
    }
}
