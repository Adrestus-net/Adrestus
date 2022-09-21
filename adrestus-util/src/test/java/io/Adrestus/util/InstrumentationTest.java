package io.Adrestus.util;

import org.junit.jupiter.api.Test;
import org.openjdk.jol.info.GraphLayout;

public class InstrumentationTest {


    @Test
    public void StressTest() {
        int loop = 10;
        SerializationUtil<Just4Test> serializationUtil = new SerializationUtil<Just4Test>(Just4Test.class);
        while (loop < 10000) {

            Just4Test o1 = new Just4Test("example", "example2");
            for (int i = 0; i < loop; i++) {
                o1.getList().add("Value" + String.valueOf(i));
            }
            long full_size = ObjectSizeCalculator.getObjectSize(o1);
            long buffer_size = full_size * 20 / 100;
            byte[] data = serializationUtil.encode(o1, (int) buffer_size);
            Just4Test copy = serializationUtil.decode(data);
            loop = loop + 10;
            //System.out.println(buffer_size);
        }

    }

    //@Test
    public void measure_bytes() {

        Just4Test o1 = new Just4Test("example", "example2");
        //Test o2=new Test("example","example2");
        int size = 3;
        for (int i = 0; i < size; i++) {
            o1.getList().add("Value" + String.valueOf(i));
        }

        System.out.println("size of int: " + ObjectSizer.retainedSize(o1));
        System.out.println("size of int: " + ObjectSizeCalculator.getObjectSize(o1));
        System.out.println("The shallow size is www: " + (GraphLayout.parseInstance(o1).totalCount()) * o1.getList().size());
        System.out.println("The shallow size is: " + GraphLayout.parseInstance(o1).toPrintable());
        // System.out.println(ClassLayout.parseClass(Just4Test.class).instanceSize());
        SerializationUtil<Just4Test> serializationUtil = new SerializationUtil<Just4Test>(Just4Test.class);
        byte[] data = serializationUtil.encode(o1, 50);
        Just4Test copy = serializationUtil.decode(data);
        System.out.println(copy.toString());
        System.out.println(copy.getList().size());

    }


}
