package io.Adrestus.core.RingBuffer.handler.receipts;

import com.google.common.reflect.TypeToken;
import io.Adrestus.core.Receipt;
import io.Adrestus.core.ReceiptBlock;
import io.Adrestus.core.RingBuffer.event.ReceiptBlockEvent;
import io.Adrestus.core.StatusType;
import io.distributedLedger.DatabaseFactory;
import io.distributedLedger.DatabaseType;
import io.distributedLedger.IDatabase;
import io.distributedLedger.LevelDBReceiptWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Optional;

public class ReplayEventHandler implements ReceiptEventHandler<ReceiptBlockEvent> {

    private static Logger LOG = LoggerFactory.getLogger(ReplayEventHandler.class);
    private final IDatabase<String, LevelDBReceiptWrapper<Receipt>> receiptdatabase;

    public ReplayEventHandler() {
        this.receiptdatabase = new DatabaseFactory(String.class, Receipt.class, new TypeToken<LevelDBReceiptWrapper<Receipt>>() {
        }.getType()).getDatabase(DatabaseType.LEVEL_DB);
    }

    @Override
    public void onEvent(ReceiptBlockEvent receiptBlockEvent, long l, boolean b) throws InterruptedException {
        ReceiptBlock receiptBlock = receiptBlockEvent.getReceiptBlock();
        try {
            ArrayList<Receipt> tosearch = receiptdatabase.findByKey(receiptBlock.getTransaction().getFrom()).get().getTo();
            Optional<Receipt> transaction_hint = tosearch.stream().filter(tr -> tr.getReceiptBlock().getHeight() == receiptBlock.getTransactionBlock().getHeight()).findFirst();

            if (transaction_hint.isPresent()) {
                receiptBlockEvent.getReceiptBlock().setStatusType(StatusType.ABORT);
                return;
            }
        } catch (NoSuchElementException e) {
            return;
        }
    }
}
