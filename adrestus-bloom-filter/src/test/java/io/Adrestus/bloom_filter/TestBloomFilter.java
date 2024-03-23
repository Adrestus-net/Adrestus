package io.Adrestus.bloom_filter;

import com.alibaba.fastjson2.JSON;
import io.Adrestus.bloom_filter.Util.UtilConstants;
import io.Adrestus.bloom_filter.core.BloomObject;
import io.Adrestus.bloom_filter.impl.InMemoryBloomFilter;
import junit.framework.Assert;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class TestBloomFilter {

    @Test
    public void testDefaultFilter() {
        BloomFilter<String> filter = new InMemoryBloomFilter<String>(10 * UtilConstants.MAX, UtilConstants.FPP);

        // generate two one-million uuid arrays
        List<String> contained = new ArrayList<String>();
        List<String> unused = new ArrayList<String>();
        for (int index = 0; index < UtilConstants.MAX; index++) {
            contained.add(UUID.randomUUID().toString());
            unused.add(UUID.randomUUID().toString());
        }

        // now add to filter
        for (String uuid : contained) {
            filter.add(uuid);
        }

        // now start checking
        for (String uuid : contained) {
            Assert.assertTrue(filter.contains(uuid));
        }
    }

    @Test
    public void testDefaultFilter2() {
        BloomFilter<String> filter1 = new InMemoryBloomFilter<String>(10 * UtilConstants.MAX, UtilConstants.FPP);
        String Adress1 = "ADR-GBIV-HG2J-27P5-BNVN-MLN6-DL5V-M3YZ-PKEJ-CFFG-FK4L";
        filter1.add(Adress1);

        BloomFilter<String> filter2 = new InMemoryBloomFilter<String>(10 * UtilConstants.MAX, UtilConstants.FPP);
        String Adress2 = "ADR-GBZX-XXCW-LWJC-J7RZ-Q6BJ-RFBA-J5WU-NBAG-4RL7-7G6Z";
        filter2.add(Adress2);

        BloomFilter<String> filter3 = new InMemoryBloomFilter<String>(10 * UtilConstants.MAX, UtilConstants.FPP);
        String Adress3 = "ADR-GD3G-DK4I-DKM2-IQSB-KBWL-HWRV-BBQA-MUAS-MGXA-5QPP";
        filter3.add(Adress3);

        BloomFilter<String> matchfilter1 = new InMemoryBloomFilter<String>(10 * UtilConstants.MAX, UtilConstants.FPP);
        matchfilter1.add(Adress1);
        int result[] = matchfilter1.toBitsetArray();

        Assert.assertTrue(filter1.containsAddress(result));
        Assert.assertFalse(filter2.containsAddress(result));
    }

    @Test
    public void testDefaultFilter3a() {
        BloomFilter<String> filter1 = new InMemoryBloomFilter<String>(10 * UtilConstants.MAX, UtilConstants.FPP);
        String Adress1 = "ADR-GBIV-HG2J-27P5-BNVN-MLN6-DL5V-M3YZ-PKEJ-CFFG-FK4L";
        filter1.add(Adress1);

        String get = Arrays.toString(filter1.toBitsetArray());
        BloomObject bloomObject = new BloomObject(filter1.toBitsetArray(), filter1.getNumberOfHashFunctions(), filter1.getNumberOfBits());
        JSONObject jsonObject = new JSONObject(bloomObject);
        String myJson = jsonObject.toString();
        BloomObject object = JSON.parseObject(myJson, BloomObject.class);

        BloomFilter<String> match_filter = new InMemoryBloomFilter<String>(object.getNumBitsRequired(), object.getHashFunctionNum(), object.getArray(), null);
        boolean res = match_filter.contains(Adress1);
        Assert.assertTrue(res);
        int g = 3;
    }

    @Test
    public void testDefaultFilter3() {
        BloomFilter<String> filter1 = new InMemoryBloomFilter<String>(100, UtilConstants.FPP);
        filter1.add(UUID.randomUUID().toString());

        for (int i = 0; i < 1000000; i++) {
            boolean val = filter1.contains(UUID.randomUUID().toString());
            // if (val) System.out.println("found");
        }
    }

    @Test
    public void testDefaultFilter4a() {
        BloomFilter<String> filter1 = new InMemoryBloomFilter<String>(20000, UtilConstants.FPP);
        for (int i = 0; i < 2000; i++) {
            filter1.add(UUID.randomUUID().toString());
        }

        int count = 0;
        for (int i = 0; i < 20000; i++) {
            boolean val = filter1.contains(UUID.randomUUID().toString());
            if (val)
                count++;
        }
        System.out.println(count);
    }

    @Test
    public void testDefaultFilter4() {

        for (int i = 0; i < 10; i++) {
            BloomFilter<String> filter1 = new InMemoryBloomFilter<String>(100, UtilConstants.FPP);
            String kl = UUID.randomUUID().toString();
            filter1.add(kl);
            BloomObject bloomObject = new BloomObject(filter1.toBitsetArray(), filter1.getNumberOfHashFunctions(), filter1.getNumberOfBits());
            JSONObject jsonObject = new JSONObject(bloomObject);
            String myJson = jsonObject.toString();
            System.out.println(kl + " " + myJson);
        }
    }

}