package io.Adrestus.bloom_filter;

import io.Adrestus.bloom_filter.Util.UtilConstants;
import io.Adrestus.bloom_filter.core.BloomObject;
import io.Adrestus.bloom_filter.impl.InMemoryBloomFilter;
import org.json.JSONObject;

public class Creation {

    public static void main(String[] args) {
        System.out.println(new Creation().create("run"));
    }

    public String create(String address) {
        BloomFilter<String> filter1 = new InMemoryBloomFilter<String>(10 * UtilConstants.MAX, UtilConstants.FPP);
        filter1.add(address);
        BloomObject bloomObject = new BloomObject(filter1.toBitsetArray(), filter1.getNumberOfHashFunctions(), filter1.getNumberOfBits());
        JSONObject jsonObject = new JSONObject(bloomObject);
        String myJson = jsonObject.toString();
        return myJson;
    }
}
