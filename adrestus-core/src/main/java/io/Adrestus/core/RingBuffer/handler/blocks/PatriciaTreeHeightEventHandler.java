package io.Adrestus.core.RingBuffer.handler.blocks;

import com.google.common.reflect.TypeToken;
import io.Adrestus.MemoryTreePool;
import io.Adrestus.core.Resourses.CachedZoneIndex;
import io.Adrestus.core.RingBuffer.event.AbstractBlockEvent;
import io.Adrestus.core.StatusType;
import io.Adrestus.core.TransactionBlock;
import io.Adrestus.mapper.MemoryTreePoolSerializer;
import io.Adrestus.util.SerializationUtil;
import io.distributedLedger.DatabaseFactory;
import io.distributedLedger.DatabaseType;
import io.distributedLedger.IDatabase;
import io.distributedLedger.ZoneDatabaseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class PatriciaTreeHeightEventHandler implements BlockEventHandler<AbstractBlockEvent> {
    private static Logger LOG = LoggerFactory.getLogger(PatriciaTreeHeightEventHandler.class);
    private final SerializationUtil patricia_tree_wrapper;

    public PatriciaTreeHeightEventHandler() {
        Type fluentType = new TypeToken<MemoryTreePool>() {
        }.getType();
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(MemoryTreePool.class, ctx -> new MemoryTreePoolSerializer()));
        List<SerializationUtil.Mapping> list2 = new ArrayList<>();
        this.patricia_tree_wrapper = new SerializationUtil<>(fluentType, list);
    }

    @Override
    public void onEvent(AbstractBlockEvent blockEvent, long l, boolean b) throws Exception {
        try {
            IDatabase<String, byte[]> tree_database = new DatabaseFactory(String.class, byte[].class).getDatabase(DatabaseType.ROCKS_DB, ZoneDatabaseFactory.getPatriciaTreeZoneInstance(CachedZoneIndex.getInstance().getZoneIndex()));
            TransactionBlock transactionBlock = (TransactionBlock) blockEvent.getBlock();
            MemoryTreePool clone = (MemoryTreePool) patricia_tree_wrapper.decode(tree_database.seekLast().get());

            if (Integer.parseInt(clone.getHeight()) != transactionBlock.getHeight() - 1) {
                LOG.info("Patricia Merkle tree height is invalid abort");
                transactionBlock.setStatustype(StatusType.ABORT);
                return;
            }
        } catch (NoSuchElementException e) {

        }
    }
}
