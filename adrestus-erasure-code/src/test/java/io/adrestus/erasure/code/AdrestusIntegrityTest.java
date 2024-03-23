package io.adrestus.erasure.code;

import io.Adrestus.erasure.code.ArrayDataDecoder;
import io.Adrestus.erasure.code.ArrayDataEncoder;
import io.Adrestus.erasure.code.EncodingPacket;
import io.Adrestus.erasure.code.Exceptions.CheckSymbolSizeOutOfBoundsException;
import io.Adrestus.erasure.code.OpenRQ;
import io.Adrestus.erasure.code.decoder.SourceBlockDecoder;
import io.Adrestus.erasure.code.encoder.SourceBlockEncoder;
import io.Adrestus.erasure.code.parameters.FECParameterObject;
import io.Adrestus.erasure.code.parameters.FECParameters;
import io.Adrestus.erasure.code.parameters.FECParametersPreConditions;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import static io.Adrestus.erasure.code.parameters.ParameterChecker.maxAllowedDataLength;
import static org.junit.Assert.assertArrayEquals;

public class AdrestusIntegrityTest {

    @Test
    public void checkDataWithSourceSymbols() {
        int numSrcBlks = 3;
        int dataLen = 6;
        int symbSize = 2;
        int symbolOverhead = 1;
        double loss = .6;
        FECParameters fecParams = FECParameters.newParameters(dataLen, symbSize, numSrcBlks);

        byte[] dst = new byte[fecParams.dataLengthAsInt()];
        byte[] data = "test".getBytes(StandardCharsets.UTF_8);
        System.arraycopy(data, 0, dst, 0, data.length);
        final ArrayDataEncoder enc = OpenRQ.newEncoder(dst, fecParams);
        final ArrayDataDecoder dec = OpenRQ.newDecoder(fecParams, 3);
        EncodingPacket srcPacket1 = enc.sourceBlockIterable().iterator().next().sourcePacketsIterable().iterator().next();
        int numRepairSymbols = OpenRQ.minRepairSymbols(dataLen / numSrcBlks, symbolOverhead, loss);
        EncodingPacket encodingPacketRepair = enc.sourceBlockIterable().iterator().next().repairPacketsIterable(numRepairSymbols).iterator().next();
//        System.out.println("a" + encodingPacketRepair.sourceBlockNumber());
        int counter = 0;
        for (SourceBlockEncoder sbEnc : enc.sourceBlockIterable()) {
            final int K = sbEnc.numberOfSourceSymbols();
//            System.out.println(sbEnc.sourceBlockNumber());
//            System.out.println(counter);
            final SourceBlockDecoder sbDec = dec.sourceBlock(counter);
            counter++;
            for (EncodingPacket srcPacket : sbEnc.sourcePacketsIterable()) {
//                System.out.println("source " + srcPacket.sourceBlockNumber());
//                System.out.println("Symbols " + srcPacket.numberOfSymbols());
//                System.out.println("Length " + srcPacket.symbolsLength());
                sbDec.putEncodingPacket(dec.parsePacket(srcPacket.asBuffer(), false).value());
            }
        }

        // compare the original and decoded data
        assertArrayEquals(dst, dec.dataArray());
    }

    @Test
    public void checkDataWithSourceSymbols1() {
        int numSrcBlks = 3;
        int dataLen = 6;
        int symbSize = 2;
        int symbolOverhead = 2;
        double loss = .6;
        FECParameters fecParams = FECParameters.newParameters(dataLen, symbSize, numSrcBlks);

        byte[] dst = new byte[fecParams.dataLengthAsInt()];
        byte[] dst2 = new byte[fecParams.dataLengthAsInt()];
        byte[] data = "test".getBytes(StandardCharsets.UTF_8);
        byte[] data2 = "data".getBytes(StandardCharsets.UTF_8);
        System.arraycopy(data, 0, dst, 0, data.length);
        System.arraycopy(data2, 0, dst2, 0, data2.length);
        final ArrayDataEncoder enc = OpenRQ.newEncoder(dst, fecParams);
        final ArrayDataEncoder enc2 = OpenRQ.newEncoder(dst2, fecParams);
        final ArrayDataDecoder dec = OpenRQ.newDecoder(fecParams, symbolOverhead);
        int counter = 0;
        ArrayList<EncodingPacket> rpairs = new ArrayList<>();
        EncodingPacket[] arr = new EncodingPacket[numSrcBlks];
        for (SourceBlockEncoder sbEnc : enc.sourceBlockIterable()) {
            int numRepairSymbols = OpenRQ.minRepairSymbols(sbEnc.numberOfSourceSymbols(), symbolOverhead, loss);
            if (numRepairSymbols > 0) {
                for (EncodingPacket encodingPacketRepair : sbEnc.repairPacketsIterable(numRepairSymbols)) {
//                    System.out.println("repair length " + encodingPacketRepair.symbolsLength());
                    rpairs.add(encodingPacketRepair);
                }
            }
            final SourceBlockDecoder sbDec = dec.sourceBlock(counter);
            for (EncodingPacket srcPacket : sbEnc.sourcePacketsIterable()) {
                arr[counter] = srcPacket;
                counter++;
                //sbDec.putEncodingPacket(dec.parsePacket(srcPacket.asBuffer(), false).value());
            }
        }

//        System.out.println(Hex.encodeHexString(arr[0].asBuffer().array()));
//        System.out.println(Hex.encodeHexString(arr[1].asBuffer().array()));
        for (int i = arr.length - 1; i >= 1; i--) {
            System.out.println("Source Block Number: " + arr[i].sourceBlockNumber());
            final SourceBlockDecoder sbDec = dec.sourceBlock(i);
            sbDec.putEncodingPacket(arr[i]);
        }

        for (EncodingPacket repair : rpairs) {
            final SourceBlockDecoder sbDec = dec.sourceBlock(repair.sourceBlockNumber());
            sbDec.putEncodingPacket(repair);
            boolean val = Arrays.equals(dst, dec.dataArray());
//            System.out.println("Repair Val: " + val);
        }
        // compare the original and decoded data
        assertArrayEquals(dst, dec.dataArray());
    }

    @Test
    public void sizeTest() throws IOException {
        Random random = new Random();

        int[] array = random.ints(100000, 10, 100000).toArray();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        for (int i = 0; i < array.length; ++i) {
            dos.writeInt(array[i]);
        }
//        System.out.println(Hex.encodeHexString(HashUtil.Shake256(baos.toByteArray())));
        int g = 3;
    }

    @Test
    public void sizeTest2() throws IOException {
        Random random = new Random();

        int[] array = random.ints(10000000, 10, 100000).toArray();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        for (int i = 0; i < array.length; ++i) {
            dos.writeInt(array[i]);
        }

        byte[] data = baos.toByteArray();
        int sizeOfCommittee = 1000;
        int numSrcBlks = 3;
        int dataLen = data.length;
        int symbSize = data.length / sizeOfCommittee;
        int symbolOverhead = sizeOfCommittee / 2;
        try {
            long MAX_DATA_LEN = maxAllowedDataLength(symbSize);
        } catch (CheckSymbolSizeOutOfBoundsException e) {
//            System.out.println(e.toString());
        }
        double loss = .6;
        FECParameters fecParams = FECParameters.newParameters(dataLen, symbSize, numSrcBlks);
        byte[] dst = new byte[fecParams.dataLengthAsInt()];
        System.arraycopy(data, 0, dst, 0, data.length);
        final ArrayDataEncoder enc = OpenRQ.newEncoder(dst, fecParams);
        final ArrayDataDecoder dec = OpenRQ.newDecoder(fecParams, symbolOverhead);
        ArrayList<EncodingPacket> set = new ArrayList<EncodingPacket>();
        for (SourceBlockEncoder sbEnc : enc.sourceBlockIterable()) {
            for (EncodingPacket srcPacket : sbEnc.sourcePacketsIterable()) {
//                System.out.println(srcPacket.sourceBlockNumber());
                set.add(srcPacket);
            }
        }
        Collections.shuffle(set);
        int g = 3;
        for (EncodingPacket encodingPacket : set) {
            final SourceBlockDecoder sbDec = dec.sourceBlock(encodingPacket.sourceBlockNumber());
            sbDec.putEncodingPacket(encodingPacket);
        }

        // compare the original and decoded data
        assertArrayEquals(dst, dec.dataArray());
    }

    @Test
    public void sizeTest3() throws IOException {
        long dataLen = 1024;
        long limit = 10124 * 1024 * 800L;
        while (dataLen < limit) {
            int sizeOfCommittee = 4;
            while (sizeOfCommittee < 200) {
                int numSrcBlks = sizeOfCommittee;
                int symbSize = (int) (dataLen / sizeOfCommittee);
                FECParameterObject object = FECParametersPreConditions.CalculateFECParameters(dataLen, symbSize, numSrcBlks);
                FECParameters.newParameters(object.getDataLen(), object.getSymbolSize(), object.getNumberOfSymbols());
                sizeOfCommittee++;
            }
            dataLen += 1024;
        }
    }

    @Test
    public void sizeTest4() throws IOException {
        long dataLen = 1024;
        int sizeOfCommittee = 100;

        int numSrcBlks = sizeOfCommittee;
        int symbSize = (int) (dataLen / sizeOfCommittee);
        FECParameterObject object = FECParametersPreConditions.CalculateFECParameters(dataLen, symbSize, numSrcBlks);
        FECParameters.newParameters(object.getDataLen(), object.getSymbolSize(), object.getNumberOfSymbols());
    }

    @Test
    public void example1() throws IOException {
        long dataLen = 1024;
        long limit = 1024 * 20 * 2L;
        while (dataLen < limit) {
            int sizeOfCommittee = 4;
            while (sizeOfCommittee < 200) {
                int numSrcBlks = sizeOfCommittee;
                int symbSize = (int) (dataLen / sizeOfCommittee);
                FECParameterObject object = FECParametersPreConditions.CalculateFECParameters(dataLen, symbSize, numSrcBlks);
                FECParameters fecParams = FECParameters.newParameters(object.getDataLen(), object.getSymbolSize(), object.getNumberOfSymbols());
                sizeOfCommittee++;

                byte[] data = new byte[fecParams.dataLengthAsInt()];
                new Random().nextBytes(data);
                final ArrayDataEncoder enc = OpenRQ.newEncoder(data, fecParams);
                final ArrayDataDecoder dec = OpenRQ.newDecoder(fecParams, object.getSymbolOverhead());
                ArrayList<EncodingPacket> set = new ArrayList<EncodingPacket>();
                for (SourceBlockEncoder sbEnc : enc.sourceBlockIterable()) {
                    for (EncodingPacket srcPacket : sbEnc.sourcePacketsIterable()) {
                        set.add(srcPacket);
                    }
                }
                //Collections.shuffle(set);
                for (EncodingPacket encodingPacket : set) {
                    final SourceBlockDecoder sbDec = dec.sourceBlock(encodingPacket.sourceBlockNumber());
                    sbDec.putEncodingPacket(encodingPacket);
                }

                // compare the original and decoded data
                assertArrayEquals(data, dec.dataArray());

            }
            dataLen += 1024;
        }
    }

    @Test
    public void exampleWithShuffle() throws IOException {
        long dataLen = 1024;
        long limit = 1024 * 10 * 2L;
        while (dataLen < limit) {
            int sizeOfCommittee = 4;
            while (sizeOfCommittee < 200) {
                int numSrcBlks = sizeOfCommittee;
                int symbSize = (int) (dataLen / sizeOfCommittee);
                FECParameterObject object = FECParametersPreConditions.CalculateFECParameters(dataLen, symbSize, numSrcBlks);
                FECParameters fecParams = FECParameters.newParameters(object.getDataLen(), object.getSymbolSize(), object.getNumberOfSymbols());
                sizeOfCommittee++;

                byte[] data = new byte[fecParams.dataLengthAsInt()];
                new Random().nextBytes(data);
                final ArrayDataEncoder enc = OpenRQ.newEncoder(data, fecParams);
                final ArrayDataDecoder dec = OpenRQ.newDecoder(fecParams, object.getSymbolOverhead());
                ArrayList<EncodingPacket> set = new ArrayList<EncodingPacket>();
                for (SourceBlockEncoder sbEnc : enc.sourceBlockIterable()) {
                    for (EncodingPacket srcPacket : sbEnc.sourcePacketsIterable()) {
                        set.add(srcPacket);
                    }
                }
                Collections.shuffle(set);
                for (EncodingPacket encodingPacket : set) {
                    final SourceBlockDecoder sbDec = dec.sourceBlock(encodingPacket.sourceBlockNumber());
                    sbDec.putEncodingPacket(encodingPacket);
                }

                // compare the original and decoded data
                assertArrayEquals(data, dec.dataArray());

            }
            dataLen += 1024;
        }
    }

    @Test
    public void exampleWithShuffleAndRepairSymbols() throws IOException {
        long dataLen = 1024;
        long limit = 1024 * 10 * 2L;
        double loss = .6;
        while (dataLen < limit) {
            int sizeOfCommittee = 4;
            while (sizeOfCommittee < 200) {
                int numSrcBlks = sizeOfCommittee;
                int symbSize = (int) (dataLen / sizeOfCommittee);
                FECParameterObject object = FECParametersPreConditions.CalculateFECParameters(dataLen, symbSize, numSrcBlks);
                FECParameters fecParams = FECParameters.newParameters(object.getDataLen(), object.getSymbolSize(), object.getNumberOfSymbols());
                sizeOfCommittee++;
                byte[] data = new byte[fecParams.dataLengthAsInt()];
                new Random().nextBytes(data);
                final ArrayDataEncoder enc = OpenRQ.newEncoder(data, fecParams);
                final ArrayDataDecoder dec = OpenRQ.newDecoder(fecParams, object.getSymbolOverhead());
                ArrayList<EncodingPacket> n = new ArrayList<EncodingPacket>();
                ArrayList<EncodingPacket> f = new ArrayList<EncodingPacket>();
                int count = 0;
                for (SourceBlockEncoder sbEnc : enc.sourceBlockIterable()) {
                    if (count < object.getNumberOfSymbols() - object.getSymbolOverhead()) {
                        for (EncodingPacket srcPacket : sbEnc.sourcePacketsIterable()) {
                            n.add(srcPacket);
                        }
                    } else {
                        int numRepairSymbols = OpenRQ.minRepairSymbols(sbEnc.numberOfSourceSymbols(), object.getSymbolOverhead(), loss);
                        if (numRepairSymbols > 0) {
                            for (EncodingPacket encodingPacketRepair : sbEnc.repairPacketsIterable(numRepairSymbols)) {
                                //System.out.println("repair length " + encodingPacketRepair.symbolsLength());
                                f.add(encodingPacketRepair);
                            }
                        }
                    }
                    count++;
                }
                Collections.shuffle(n);
                for (int i = 0; i < n.size(); i++) {
                    final SourceBlockDecoder sbDec = dec.sourceBlock(n.get(i).sourceBlockNumber());
                    sbDec.putEncodingPacket(n.get(i));
                }

                count = 0;
                for (EncodingPacket repair : f) {
                    final SourceBlockDecoder sbDec = dec.sourceBlock(repair.sourceBlockNumber());
                    sbDec.putEncodingPacket(repair);
                    boolean val = Arrays.equals(data, dec.dataArray());
                    if (val) {
                        break;
                    }
                    count++;
                }

                // compare the original and decoded data
                assertArrayEquals(data, dec.dataArray());
            }
            dataLen += 1024;
        }
    }
}
