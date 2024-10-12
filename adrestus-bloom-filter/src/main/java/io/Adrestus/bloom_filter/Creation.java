package io.Adrestus.bloom_filter;

import com.alibaba.fastjson2.JSON;
import io.Adrestus.bloom_filter.Util.UtilConstants;
import io.Adrestus.bloom_filter.core.BloomObject;
import io.Adrestus.bloom_filter.impl.InMemoryBloomFilter;

public class Creation {

    public static void main(String[] args) {
        System.out.println(new Creation().create("run"));
    }

    public String create(String address) {
        BloomFilter<String> filter1 = new InMemoryBloomFilter<String>(10 * UtilConstants.MAX, UtilConstants.FPP);
        filter1.add(address);
        BloomObject bloomObject = new BloomObject(filter1.toBitsetArray(), filter1.getNumberOfHashFunctions(), filter1.getNumberOfBits());
        String myJson = JSON.toJSONString(bloomObject);
        return myJson;
    }
}
