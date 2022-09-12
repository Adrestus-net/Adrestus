package io.Adrestus.crypto.bls.utils;


import io.Adrestus.crypto.bls.BLS381.BIG;
import io.Adrestus.crypto.bls.model.FieldElement;


import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class CommonUtils {
    private static final long MAX_BATCH_VERIFY_RANDOM_MULTIPLIER = Long.MAX_VALUE;

    private static Random getRND() {
        return ThreadLocalRandom.current();
    }

    public static int padCollection(List<List<Integer>> coll, int pad) {
        if (coll.size() > 0) {
            int maxLength = coll.get(0).size();
            for (int i = 1; i < coll.size(); i++) {
                if (maxLength < coll.get(i).size()) {
                    maxLength = coll.get(i).size();
                }
            }

            for (int i = 0; i < coll.size(); i++) {
                List<Integer> col = coll.get(i);
                for (int j = col.size(); j < maxLength; j++) {
                    col.add(pad);
                }
            }
            return maxLength;
        }
        return 0;
    }


    public static FieldElement nextBatchRandomMultiplier() {
        long randomLong =
                (getRND().nextLong() & 0x7fffffffffffffffL) % MAX_BATCH_VERIFY_RANDOM_MULTIPLIER;
        BIG randomBig = longToBIG(randomLong);
        return new FieldElement(randomBig);
    }

    public static BIG longToBIG(long l) {
        long[] bigContent = new long[BIG.NLEN];
        bigContent[0] = l;
        return new BIG(bigContent);
    }

}
