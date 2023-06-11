package io.Adrestus.core.RingBuffer.handler.transactions;

import io.Adrestus.config.SocketConfigOptions;
import io.Adrestus.core.Resourses.CachedLatestBlocks;
import io.Adrestus.core.Resourses.CachedZoneIndex;
import io.Adrestus.core.RingBuffer.event.TransactionEvent;
import io.Adrestus.core.Transaction;
import io.Adrestus.crypto.elliptic.mapper.BigIntegerSerializer;
import io.Adrestus.network.AsyncService;
import io.Adrestus.util.SerializationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BroadcastTransactionHandler extends TransactionEventHandler{

    private static Logger LOG = LoggerFactory.getLogger(BroadcastTransactionHandler.class);
    private SerializationUtil<Transaction> wrapper;

    public BroadcastTransactionHandler() {
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        wrapper = new SerializationUtil<Transaction>(Transaction.class, list);
    }

    @Override
    public void onEvent(TransactionEvent transactionEvent, long l, boolean b) throws Exception {
        Transaction transaction = transactionEvent.getTransaction();

        List<String> ips = CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(CachedZoneIndex.getInstance().getZoneIndex()).values().stream().collect(Collectors.toList());
        var executor = new AsyncService<Long>(ips,  wrapper.encode(transaction, 1024), SocketConfigOptions.TRANSACTION_PORT);

        var asyncResult1 = executor.startProcess(300L);
        final var result1 = executor.endProcess(asyncResult1);
    }
}
