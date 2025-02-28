package io.Adrestus.core.BlockPipeline.FlowChain.Committee;

import io.Adrestus.core.BlockPipeline.BlockRequest;
import io.Adrestus.core.BlockPipeline.BlockRequestHandler;
import io.Adrestus.core.BlockPipeline.BlockRequestType;
import io.Adrestus.core.CommitteeBlock;
import io.Adrestus.core.Resourses.CachedSecurityHeaders;
import io.Adrestus.core.StatusType;
import io.Adrestus.util.GetTime;
import io.Adrestus.util.MathOperationUtil;
import io.distributedLedger.DatabaseFactory;
import io.distributedLedger.DatabaseInstance;
import io.distributedLedger.DatabaseType;
import io.distributedLedger.IDatabase;
import lombok.SneakyThrows;
import org.spongycastle.util.encoders.Hex;

import java.util.ArrayList;
import java.util.Map;

public class ForgeDifficultyBuilder implements BlockRequestHandler<CommitteeBlock> {
    private final IDatabase<String, CommitteeBlock> database;

    public ForgeDifficultyBuilder() {
        database = new DatabaseFactory(String.class, CommitteeBlock.class).getDatabase(DatabaseType.ROCKS_DB, DatabaseInstance.COMMITTEE_BLOCK);
    }

    @Override
    public boolean canHandleRequest(BlockRequest<CommitteeBlock> req) {
        return req.getRequestType() == BlockRequestType.FORGE_DIFFICULTY_BUILDER;
    }

    @Override
    public int getPriority() {
        return 2;
    }

    @Override
    @SneakyThrows
    public void process(BlockRequest<CommitteeBlock> blockRequest) {
        int finish = database.findDBsize();

        int n = finish;
        int summdiffuclty = 0;
        long sumtime = 0;
        Map<String, CommitteeBlock> block_entries = database.seekBetweenRange(0, finish);
        ArrayList<String> entries = new ArrayList<String>(block_entries.keySet());

        if (entries.size() == 1) {
            summdiffuclty = block_entries.get(entries.get(0)).getDifficulty();
            sumtime = 100;
        } else {
            for (int i = 0; i < entries.size(); i++) {
                if (i == entries.size() - 1)
                    break;

                long older = GetTime.GetTimestampFromString(block_entries.get(entries.get(i)).getHeaderData().getTimestamp()).toEpochMilli();
                long newer = GetTime.GetTimestampFromString(block_entries.get(entries.get(i + 1)).getHeaderData().getTimestamp()).toEpochMilli();
                sumtime = sumtime + (newer - older);
                //System.out.println("edw "+(newer - older));
                summdiffuclty = summdiffuclty + block_entries.get(entries.get(i)).getDifficulty();
                //  System.out.println("edw "+(newer - older));
            }
        }

        double d = ((double) summdiffuclty / n);
        // String s=String.format("%4d",  sumtime / n);
        double t = ((double) sumtime / n);
        //  System.out.println(t);
        int difficulty = MathOperationUtil.multiplication((int) Math.round((t) / d));
        if (difficulty < 100) {
            blockRequest.getBlock().setStatustype(StatusType.ABORT);
            throw new IllegalArgumentException("VDF difficulty is not set correct abort");
        }
        blockRequest.getBlock().setDifficulty(difficulty);
        blockRequest.getBlock().setVDF(Hex.toHexString(CachedSecurityHeaders.getInstance().getSecurityHeader().getRnd()));
    }

    @Override
    public String name() {
        return "ForgeDifficultyBuilder";
    }

    @Override
    public void clear(BlockRequest<CommitteeBlock> blockRequest) {
        blockRequest.clear();
    }
}
