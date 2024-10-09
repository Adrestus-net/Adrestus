package io.Adrestus.core.RingBuffer.handler.receipts;

import io.Adrestus.config.SocketConfigOptions;
import io.Adrestus.core.Receipt;
import io.Adrestus.core.ReceiptBlock;
import io.Adrestus.core.Resourses.CachedLatestBlocks;
import io.Adrestus.core.Resourses.CachedZoneIndex;
import io.Adrestus.core.RingBuffer.event.ReceiptBlockEvent;
import io.Adrestus.core.StatusType;
import io.Adrestus.crypto.bls.BLS381.ECP;
import io.Adrestus.crypto.bls.BLS381.ECP2;
import io.Adrestus.crypto.bls.mapper.ECP2mapper;
import io.Adrestus.crypto.bls.mapper.ECPmapper;
import io.Adrestus.crypto.elliptic.mapper.BigDecimalSerializer;
import io.Adrestus.crypto.elliptic.mapper.BigIntegerSerializer;
import io.Adrestus.crypto.elliptic.mapper.CustomSerializerTreeMap;
import io.Adrestus.network.AsyncService;
import io.Adrestus.util.SerializationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class ZoneToEventHandler implements ReceiptEventHandler<ReceiptBlockEvent> {
    private final SerializationUtil<Receipt> receipt_encode;
    private static Logger LOG = LoggerFactory.getLogger(ZoneToEventHandler.class);

    public ZoneToEventHandler() {
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
        list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
        list.add(new SerializationUtil.Mapping(BigDecimal.class, ctx -> new BigDecimalSerializer()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        list.add(new SerializationUtil.Mapping(TreeMap.class, ctx -> new CustomSerializerTreeMap()));
        this.receipt_encode = new SerializationUtil<Receipt>(Receipt.class, list);
    }

    @Override
    public void onEvent(ReceiptBlockEvent receiptBlockEvent, long l, boolean b) throws InterruptedException {
        ReceiptBlock receiptBlock = receiptBlockEvent.getReceiptBlock();
        if (receiptBlock.getReceipt().getZoneTo() != CachedZoneIndex.getInstance().getZoneIndex()) {
            LOG.info("Receipt Block zone are not valid Sending it to the correct zone and abort");
            receiptBlockEvent.getReceiptBlock().setStatusType(StatusType.ABORT);
            List<String> ReceiptIPWorkers = CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(receiptBlock.getReceipt().getZoneTo()).values().stream().collect(Collectors.toList());
            if (!ReceiptIPWorkers.isEmpty()) {
                List<byte[]> toSendReceipt = new ArrayList<>();

                toSendReceipt.add(receipt_encode.encode(receiptBlock.getReceipt(), 1024));
                var executor = new AsyncService<Long>(ReceiptIPWorkers, toSendReceipt, SocketConfigOptions.RECEIPT_PORT);
                var asyncResult = executor.startListProcess(300L);
                var result = executor.endProcess(asyncResult);
            }
            return;
        }
    }
}
