package io.Adrestus.core.RingBuffer.handler.blocks;

import io.Adrestus.config.AdrestusConfiguration;
import io.Adrestus.core.CommitteeBlock;
import io.Adrestus.core.RingBuffer.event.AbstractBlockEvent;
import io.Adrestus.core.StatusType;
import io.Adrestus.util.GetTime;
import io.distributedLedger.DatabaseFactory;
import io.distributedLedger.DatabaseType;
import io.distributedLedger.IDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Map;

public class DifficultyEventHandler implements BlockEventHandler<AbstractBlockEvent> {

    private static Logger LOG = LoggerFactory.getLogger(DifficultyEventHandler.class);
    private final IDatabase<String, CommitteeBlock> database;

    public DifficultyEventHandler() {
        this.database = new DatabaseFactory(String.class, CommitteeBlock.class).getDatabase(DatabaseType.ROCKS_DB);
    }

    @Override
    public void onEvent(AbstractBlockEvent blockEvent, long l, boolean b) throws Exception {
        CommitteeBlock block = (CommitteeBlock) blockEvent.getBlock();
        int finish = database.findDBsize();
        if (finish == 0)
            return;

        int n = finish;
        int summdiffuclty = 0;
        long sumtime = 0;
        Map<String, CommitteeBlock> block_entries = database.seekBetweenRange(0,finish);
        ArrayList<String> entries = new ArrayList<String>(block_entries.keySet());

        if(finish==1){
            sumtime = GetTime.GetTimestampFromString(block_entries.get(entries.get(0)).getHeaderData().getTimestamp()).getTime();
            summdiffuclty = block_entries.get(entries.get(0)).getDifficulty();
        }
        for (int i = 0; i < entries.size(); i++) {
            if (i == entries.size() - 1)
                break;

            long older = GetTime.GetTimestampFromString(block_entries.get(entries.get(i)).getHeaderData().getTimestamp()).getTime();
            long newer = GetTime.GetTimestampFromString(block_entries.get(entries.get(i + 1)).getHeaderData().getTimestamp()).getTime();
            sumtime = sumtime + (newer - older);
            //System.out.println("edw "+(newer - older));
            summdiffuclty = summdiffuclty + block_entries.get(entries.get(i)).getDifficulty();
            //  System.out.println("edw "+(newer - older));
            if ((newer - older) > 1000) {
                int h = i;
            }
        }

        double d = ((double) summdiffuclty / n);
        // String s=String.format("%4d",  sumtime / n);
        double t = ((double) sumtime / n);
        //  System.out.println(t);
        int difficulty = (int)Math.round(t * ((double) AdrestusConfiguration.INIT_VDF_DIFFICULTY / d));
        difficulty=difficulty*2;

        if(difficulty!=block.getDifficulty()){
            LOG.info("VDF Difficulty not match");
            block.setStatustype(StatusType.ABORT);
            return;
        }
    }
}
