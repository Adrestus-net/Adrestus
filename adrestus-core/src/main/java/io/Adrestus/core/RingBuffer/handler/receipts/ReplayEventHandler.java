package io.Adrestus.core.RingBuffer.handler.receipts;

import io.Adrestus.TreeFactory;
import io.Adrestus.Trie.StorageInfo;
import io.Adrestus.core.Receipt;
import io.Adrestus.core.ReceiptBlock;
import io.Adrestus.core.Resourses.CachedZoneIndex;
import io.Adrestus.core.RingBuffer.event.ReceiptBlockEvent;
import io.Adrestus.core.StatusType;
import io.Adrestus.core.TransactionBlock;
import io.Adrestus.crypto.HashUtil;
import io.Adrestus.crypto.elliptic.mapper.BigIntegerSerializer;
import io.Adrestus.util.SerializationUtil;
import io.distributedLedger.DatabaseFactory;
import io.distributedLedger.DatabaseType;
import io.distributedLedger.IDatabase;
import io.distributedLedger.ZoneDatabaseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class ReplayEventHandler implements ReceiptEventHandler<ReceiptBlockEvent> {

    private static Logger LOG = LoggerFactory.getLogger(ReplayEventHandler.class);

    private final SerializationUtil<Receipt> recep;

    public ReplayEventHandler() {
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        this.recep = new SerializationUtil<Receipt>(Receipt.class, list);
    }

    @Override
    public void onEvent(ReceiptBlockEvent receiptBlockEvent, long l, boolean b) throws InterruptedException {
        ReceiptBlock receiptBlock = receiptBlockEvent.getReceiptBlock();
        ArrayList<StorageInfo> rcp;
        try {
            String hashToSearch = HashUtil.sha256_bytetoString(recep.encode(receiptBlock.getReceipt()));
            rcp = (ArrayList<StorageInfo>) TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex()).getByaddress(receiptBlock.getTransaction().getFrom()).get().retrieveReceiptInfoByHash(hashToSearch);
        } catch (NoSuchElementException e) {
            return;
        }
        for (int i = 0; i < rcp.size(); i++) {
            try {
                IDatabase<String, TransactionBlock> transactionBlockIDatabase = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, ZoneDatabaseFactory.getZoneInstance(rcp.get(i).getOrigin_zone()));
                int finalI = i;
                Receipt recovered = transactionBlockIDatabase.findByKey(String.valueOf(rcp.get(i).getBlockHeight())).get().getInbound().getMap_receipts().get(rcp.get(i).getZone()).entrySet().stream().filter(entry -> entry.getKey().getHeight() == rcp.get(finalI).getReceiptBlockHeight()).findFirst().get().getValue().get(rcp.get(i).getPosition());
                if (recovered.equals(receiptBlock.getReceipt())) {
                    receiptBlockEvent.getReceiptBlock().setStatusType(StatusType.ABORT);
                    return;
                }
            } catch (NoSuchElementException e) {
            } catch (IndexOutOfBoundsException e) {
            }
        }
    }
}
