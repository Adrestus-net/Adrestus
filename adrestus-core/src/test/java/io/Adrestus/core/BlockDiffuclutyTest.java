package io.Adrestus.core;

import io.Adrestus.util.GetTime;
import io.distributedLedger.DatabaseFactory;
import io.distributedLedger.DatabaseType;
import io.distributedLedger.IDatabase;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Map;

public class BlockDiffuclutyTest {


    @Test
    public void test_difficulty() throws ParseException, InterruptedException {

        IDatabase<String, CommitteeBlock> database = new DatabaseFactory(String.class, CommitteeBlock.class).getDatabase(DatabaseType.ROCKS_DB);
        int MAX_ITERATIONS = 100;
        int iterations = 0;

        CommitteeBlock bootsrap = new CommitteeBlock();
        bootsrap.setHash(String.valueOf(0));
        bootsrap.getHeaderData().setTimestamp(GetTime.GetTimeStampInString());
        bootsrap.setDifficulty((int) Math.pow(10, 2));
        database.save(bootsrap.getHash(), bootsrap);

        Thread.sleep(200);

        while (iterations < MAX_ITERATIONS) {
            Map<String, CommitteeBlock> results = database.findBetweenRange(bootsrap.getHash());
            ArrayList<String> keys = new ArrayList<String>(results.keySet());
            int n = keys.size();
            int summdiffuclty = 0;
            long sumtime = 0;
            if (iterations == 0) {
                n = 1;
                summdiffuclty = bootsrap.getDifficulty();
                sumtime = GetTime.GetTimeStamp().getTime() - GetTime.GetTimestampFromString(bootsrap.getHeaderData().getTimestamp()).getTime();
            } else if (iterations == 1) {
                long older = GetTime.GetTimestampFromString(results.get(keys.get(0)).getHeaderData().getTimestamp()).getTime();
                Thread.sleep(200);
                long newer = GetTime.GetTimeStamp().getTime();
                sumtime = sumtime + (newer - older);
                summdiffuclty = summdiffuclty + results.get(keys.get(0)).getDifficulty();
            }
            for (int i = 0; i < keys.size(); i++) {
                if (i == keys.size() - 1)
                    break;

                long older = GetTime.GetTimestampFromString(results.get(keys.get(i)).getHeaderData().getTimestamp()).getTime();
                long newer = GetTime.GetTimestampFromString(results.get(keys.get(i + 1)).getHeaderData().getTimestamp()).getTime();
                sumtime = sumtime + (newer - older);
                //System.out.println("edw "+(newer - older));
                summdiffuclty = summdiffuclty + results.get(keys.get(i)).getDifficulty();
                //  System.out.println("edw "+(newer - older));
                if ((newer - older) > 1000) {
                    int h = i;
                }
            }

            double d = ((double) summdiffuclty / n);
            // String s=String.format("%4d",  sumtime / n);
            double t = ((double) sumtime / n);
            //  System.out.println(t);
            double difficulty = t * ((double) 100 / d);

            Thread.sleep(200);
            CommitteeBlock block = new CommitteeBlock();
            block.setHash(String.valueOf(iterations));
            block.getHeaderData().setTimestamp(GetTime.GetTimeStampInString());
            block.setDifficulty((int) Math.round(difficulty));

            System.out.println("Difficulty is: " + block.getDifficulty() + " Number of blocks is: " + n);
            database.save(block.getHash(), block);
            iterations++;
        }
        database.deleteAll();
    }

}
