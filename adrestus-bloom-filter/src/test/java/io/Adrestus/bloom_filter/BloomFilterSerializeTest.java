package io.Adrestus.bloom_filter;

import com.google.common.reflect.TypeToken;
import io.Adrestus.bloom_filter.Util.UtilConstants;
import io.Adrestus.bloom_filter.impl.InMemoryBloomFilter;
import io.Adrestus.bloom_filter.mapper.BloomFilterSerializer;
import io.Adrestus.util.SerializationUtil;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BloomFilterSerializeTest {


    @Test
    public void test_serialize() {
        BloomFilter<String> filter1 = new InMemoryBloomFilter<String>(10 * UtilConstants.MAX, UtilConstants.FPP);
        ArrayList<String> lists = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            String s = UUID.randomUUID().toString();
            lists.add(s);
            filter1.add(s);
        }
        Type fluentType = new TypeToken<BloomFilter>() {
        }.getType();
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(BloomFilter.class, ctx -> new BloomFilterSerializer()));
        SerializationUtil<BloomFilter> ser = new SerializationUtil<BloomFilter>(fluentType, list);
        byte[] data = ser.encode(filter1);
        BloomFilter<String> copy = ser.decode(data);
        assertEquals(filter1, copy);
        for (int i = 0; i < 100; i++) {
            assertEquals(true, copy.contains(lists.get(i)));
        }
        //assertEquals(filter1, copy);
    }
}
