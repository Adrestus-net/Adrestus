package io.Adrestus.util;

import java.security.SecureRandom;
import java.util.ArrayList;

public final class CustomRandom {


    public static int generateRandom(SecureRandom rand, int start, int end, ArrayList<Integer> excludeRows) {
        int range = end - start + 1;
        int random = rand.nextInt(range);
        while (excludeRows.contains(random)) {
            random = rand.nextInt(range);
        }

        return random;
    }
}
