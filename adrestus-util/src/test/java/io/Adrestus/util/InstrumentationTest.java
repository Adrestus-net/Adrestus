package io.Adrestus.util;

public class InstrumentationTest {
    static {
        ObjectSizeCalculator.disableAccessWarnings();
    }

//    // @Test
//    public void StressTest() throws InterruptedException {
//        int loop = 10;
//        SerializationUtil<Just4Test> serializationUtil = new SerializationUtil<Just4Test>(Just4Test.class);
//        while (loop < 11) {
//
//            Just4Test o1 = new Just4Test("example", "example2");
//            for (int i = 0; i < loop; i++) {
//                o1.getList().add("Value" + i);
//            }
//            long full_size = ObjectSizeCalculator.getObjectSize(o1);
//            int buffer_size = (int) full_size;
//            byte[] data = serializationUtil.encode(o1);
//            Just4Test copy = serializationUtil.decode(data);
//            loop = loop + 10;
//        }
//
//    }

//    @Test
//    public void measure_bytes() throws Exception {
//        SerializationUtil<Just4Test> serializationUtils = new SerializationUtil<Just4Test>(Just4Test.class);
//        Just4Test o1 = new Just4Test("example", "example2");
//        //Test o2=new Test("example","example2");
//        int size = 3;
//        for (int i = 0; i < size; i++) {
//            o1.getList().add("Value" + i);
//        }
//
//        //ObjectSizer.getUnsafe();
//        //ObjectSizer.disableAccessWarnings();
//        System.out.println("size of int: " + ObjectSizer.retainedSize(o1));
//        System.out.println("size of int: " + ObjectSizeCalculator.getObjectSize(o1));
//        System.out.println("size of int: " + ClassLayout.parseInstance(o1).toPrintable());
//        System.out.println("The shallow size is www: " + (GraphLayout.parseInstance(o1).totalCount()) * o1.getList().size());
//        System.out.println("The shallow size is: " + GraphLayout.parseInstance(o1).toPrintable());
//        // System.out.println(ClassLayout.parseClass(Just4Test.class).instanceSize());
//        SerializationUtil<Just4Test> serializationUtil = new SerializationUtil<Just4Test>(Just4Test.class);
//        byte[] data = serializationUtil.encode(o1, 50);
//        Just4Test copy = serializationUtil.decode(data);
//        System.out.println(copy.toString());
//        System.out.println(copy.getList().size());
//
//    }


}
