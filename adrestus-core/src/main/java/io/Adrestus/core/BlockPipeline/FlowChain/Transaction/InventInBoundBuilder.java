package io.Adrestus.core.BlockPipeline.FlowChain.Transaction;

import com.google.common.reflect.TypeToken;
import io.Adrestus.MemoryTreePool;
import io.Adrestus.TreeFactory;
import io.Adrestus.Trie.PatriciaTreeTransactionType;
import io.Adrestus.core.BlockPipeline.BlockRequest;
import io.Adrestus.core.BlockPipeline.BlockRequestHandler;
import io.Adrestus.core.BlockPipeline.BlockRequestType;
import io.Adrestus.core.Receipt;
import io.Adrestus.core.Resourses.CachedInboundTransactionBlocks;
import io.Adrestus.core.Resourses.CachedZoneIndex;
import io.Adrestus.core.Resourses.MemoryReceiptPool;
import io.Adrestus.core.Transaction;
import io.Adrestus.core.TransactionBlock;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class InventInBoundBuilder implements BlockRequestHandler<TransactionBlock> {
    private static final Logger log = LoggerFactory.getLogger(InventInBoundBuilder.class);
    private final SerializationUtil<Receipt> receipt_encode;

    public InventInBoundBuilder() {
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
        return req.getRequestType() == BlockRequestType.INVENT_INBOUND_BUILDER;
    }

    @Override
    public int getPriority() {
        return 2;
    }

    @Override
    @SneakyThrows
    public void process(BlockRequest<TransactionBlock> blockRequest) {
        if (!blockRequest.getBlock().getInbound().getMap_receipts().isEmpty()) {
            blockRequest.getBlock()
                    .getInbound()
                    .getMap_receipts()
                    .forEach((key, value) -> value
                            .entrySet()
                            .stream()
                            .forEach(entry -> {
                                for (int i = 0; i < entry.getValue().size(); i++) {
                                    Receipt receipt = entry.getValue().get(i);
                                    TransactionBlock block = CachedInboundTransactionBlocks.getInstance().retrieve(receipt.getZoneFrom(), receipt.getReceiptBlock().getHeight());
                                    Transaction trx = block.getTransactionList().get(receipt.getPosition());
                                    String rcphash = HashUtil.sha256_bytetoString(this.receipt_encode.encode(receipt, 1024));
                                    TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(trx.getTo()).get().addReceiptPosition(rcphash, CachedZoneIndex.getInstance().getZoneIndex(), blockRequest.getBlock().getHeight(), receipt.getZoneFrom(), receipt.getReceiptBlock().getHeight(), i);
                                    TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).deposit(PatriciaTreeTransactionType.REGULAR, trx.getTo(), trx.getAmount(), trx.getAmountWithTransactionFee());
                                    MemoryReceiptPool.getInstance().delete(receipt);
                                }
                            }));
        }

    }

    @Override
    public void clear(BlockRequest<TransactionBlock> blockRequest) {
        blockRequest.clear();
    }

    @Override
    public String name() {
        return "InventInBoundBuilder";
    }
}

