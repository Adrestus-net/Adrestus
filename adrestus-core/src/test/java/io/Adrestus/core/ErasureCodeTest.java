package io.Adrestus.core;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import io.Adrestus.config.AdrestusConfiguration;
import io.Adrestus.core.Resourses.CachedZoneIndex;
import io.Adrestus.core.Util.BlockSizeCalculator;
import io.Adrestus.crypto.HashUtil;
import io.Adrestus.crypto.WalletAddress;
import io.Adrestus.crypto.bls.BLS381.ECP;
import io.Adrestus.crypto.bls.BLS381.ECP2;
import io.Adrestus.crypto.bls.mapper.ECP2mapper;
import io.Adrestus.crypto.bls.mapper.ECPmapper;
import io.Adrestus.crypto.bls.model.BLSPrivateKey;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.bls.model.CachedBLSKeyPair;
import io.Adrestus.crypto.elliptic.ECDSASign;
import io.Adrestus.crypto.elliptic.ECDSASignatureData;
import io.Adrestus.crypto.elliptic.ECKeyPair;
import io.Adrestus.crypto.elliptic.Keys;
import io.Adrestus.crypto.elliptic.mapper.BigIntegerSerializer;
import io.Adrestus.crypto.elliptic.mapper.CustomSerializerTreeMap;
import io.Adrestus.crypto.mnemonic.Mnemonic;
import io.Adrestus.crypto.mnemonic.MnemonicException;
import io.Adrestus.crypto.mnemonic.Security;
import io.Adrestus.crypto.mnemonic.WordList;
import io.Adrestus.erasure.code.*;
import io.Adrestus.erasure.code.decoder.SourceBlockDecoder;
import io.Adrestus.erasure.code.encoder.SourceBlockEncoder;
import io.Adrestus.erasure.code.parameters.FECParameterObject;
import io.Adrestus.erasure.code.parameters.FECParameters;
import io.Adrestus.erasure.code.parameters.FECParametersPreConditions;
import io.Adrestus.util.GetTime;
import io.Adrestus.util.SerializationUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ErasureCodeTest {

    private static final int TRANSACTION_SIZE = 100;

    private static BLSPrivateKey sk1;
    private static BLSPublicKey vk1;
    private static TransactionBlock transactionBlock;
    private static SerializationUtil<TransactionBlock> encode;
    private static SerializationUtil<Transaction> serenc;
    private static SerializationUtil<SerializableErasureObject> serenc_erasure;

    @BeforeAll
    public static void setup() throws MnemonicException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException, CloneNotSupportedException {
        sk1 = new BLSPrivateKey(1);
        vk1 = new BLSPublicKey(sk1);
        CachedBLSKeyPair.getInstance().setPublicKey(vk1);
        CachedBLSKeyPair.getInstance().setPrivateKey(sk1);

        CachedZoneIndex.getInstance().setZoneIndex(0);
        ECDSASign ecdsaSign = new ECDSASign();

        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
        list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        list.add(new SerializationUtil.Mapping(TreeMap.class, ctx -> new CustomSerializerTreeMap()));
        encode = new SerializationUtil<TransactionBlock>(TransactionBlock.class, list);
        serenc = new SerializationUtil<Transaction>(Transaction.class, list);
        serenc_erasure = new SerializationUtil<SerializableErasureObject>(SerializableErasureObject.class);

        ArrayList<String> addreses = new ArrayList<>();
        ArrayList<Transaction> transactions = new ArrayList<>();
        ArrayList<ECKeyPair> keypair = new ArrayList<>();
        int version = 0x00;
        for (int i = 0; i < TRANSACTION_SIZE; i++) {
            Mnemonic mnem = new Mnemonic(Security.NORMAL, WordList.ENGLISH);
            char[] mnemonic_sequence = mnem.create();
            char[] passphrase = "p4ssphr4se".toCharArray();
            byte[] key = mnem.createSeed(mnemonic_sequence, passphrase);
            ECKeyPair ecKeyPair = Keys.createEcKeyPair(new SecureRandom(key));
            String adddress = WalletAddress.generate_address((byte) version, ecKeyPair.getPublicKey());
            addreses.add(adddress);
            keypair.add(ecKeyPair);
        }


        for (int i = 0; i < TRANSACTION_SIZE - 1; i++) {
            Transaction transaction = new RegularTransaction();
            transaction.setFrom(addreses.get(i));
            transaction.setTo(addreses.get(i + 1));
            transaction.setStatus(StatusType.PENDING);
            transaction.setTimestamp(GetTime.GetTimeStampInString());
            transaction.setZoneFrom(1);
            transaction.setZoneTo(2);
            transaction.setAmount(100);
            transaction.setAmountWithTransactionFee(transaction.getAmount() * (10.0 / 100.0));
            transaction.setNonce(1);
            byte byf[] = serenc.encode(transaction);
            transaction.setHash(HashUtil.sha256_bytetoString(byf));
            await().atMost(500, TimeUnit.MILLISECONDS);

            ECDSASignatureData signatureData = ecdsaSign.secp256SignMessage(Hex.decode(transaction.getHash()), keypair.get(i));
            transaction.setSignature(signatureData);
            //MemoryPool.getInstance().add(transaction);
            transactions.add(transaction);
            await().atMost(1000, TimeUnit.MILLISECONDS);
        }
        transactionBlock = new TransactionBlock();
        transactionBlock.getHeaderData().setPreviousHash("4c89512018237bd0cd458b50ffbf47190fc7aa7d4430093418fa7927a9a46ac3");
        transactionBlock.getHeaderData().setVersion(AdrestusConfiguration.version);
        transactionBlock.getHeaderData().setTimestamp(GetTime.GetTimeStampInString());
        transactionBlock.setStatustype(StatusType.PENDING);
        transactionBlock.setHeight(1);
        transactionBlock.setGeneration(1);
        transactionBlock.setViewID(1);
        transactionBlock.setZone(CachedZoneIndex.getInstance().getZoneIndex());
        transactionBlock.setLeaderPublicKey(CachedBLSKeyPair.getInstance().getPublicKey());
        transactionBlock.setTransactionList(transactions);
        transactionBlock.setHash("hash10");
        transactionBlock.setSize(1);
        transactionBlock.setPatriciaMerkleRoot("1d51602355c8255d11baf4915c500a92e9d027f478dfa2286ee509a7469c08ab");
        transactionBlock.setHash("1d51602355c8255d11baf4915c500a92e9d027f478dfa2286ee509a7469c08ab");

        Receipt.ReceiptBlock receiptBlock = new Receipt.ReceiptBlock(transactionBlock.getHash(), transactionBlock.getHeight(), transactionBlock.getGeneration(), transactionBlock.getMerkleRoot());
        ArrayList<Receipt> receiptList = new ArrayList<>();
        for (int i = 0; i < transactionBlock.getTransactionList().size(); i++) {
            Transaction transaction = transactionBlock.getTransactionList().get(i);
            receiptList.add(new Receipt(transaction.getZoneFrom(), transaction.getZoneTo(), transaction.getTo(), transaction.getAmount(), receiptBlock, (Transaction) transaction.clone(), null, i));
        }

        Map<Integer, Map<Receipt.ReceiptBlock, List<Receipt>>> outbound = receiptList
                .stream()
                .collect(Collectors.groupingBy(Receipt::getZoneTo, Collectors.groupingBy(Receipt::getReceiptBlock)));

        OutBoundRelay outBoundRelay = new OutBoundRelay(outbound);
        transactionBlock.setOutbound(outBoundRelay);
    }

    @Test
    public void Serializationtest() throws MnemonicException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException, CloneNotSupportedException {
        BlockSizeCalculator sizeCalculator = new BlockSizeCalculator();
        sizeCalculator.setTransactionBlock(transactionBlock);
        byte[] buffer = encode.encode(transactionBlock, sizeCalculator.TransactionBlockSizeCalculator());

        long dataLen = buffer.length;
        int sizeOfCommittee = 4;

        int numSrcBlks = sizeOfCommittee;
        int symbSize = (int) (dataLen / sizeOfCommittee);
        FECParameterObject object = FECParametersPreConditions.CalculateFECParameters(dataLen, symbSize, numSrcBlks);
        FECParameters fecParams = FECParameters.newParameters(object.getDataLen(), object.getSymbolSize(), object.getNumberOfSymbols());

        byte[] data = new byte[fecParams.dataLengthAsInt()];
        System.arraycopy(buffer, 0, data, 0, data.length);
        final ArrayDataEncoder enc = OpenRQ.newEncoder(data, fecParams);
        ArrayList<SerializableErasureObject> serializableErasureObjects = new ArrayList<SerializableErasureObject>();
        for (SourceBlockEncoder sbEnc : enc.sourceBlockIterable()) {
            for (EncodingPacket srcPacket : sbEnc.sourcePacketsIterable()) {
                serializableErasureObjects.add(new SerializableErasureObject(object, srcPacket.asArray()));
            }
        }
        ArrayList<byte[]> toSend = new ArrayList<>();
        for (SerializableErasureObject obj : serializableErasureObjects) {
            toSend.add(serenc_erasure.encode(obj));
        }
        //#########################################################################################################################
        ArrayList<SerializableErasureObject> recserializableErasureObjects = new ArrayList<SerializableErasureObject>();
        for (byte[] rec_buff : toSend) {
            recserializableErasureObjects.add(serenc_erasure.decode(rec_buff));
        }
        assertEquals(serializableErasureObjects, recserializableErasureObjects);
        Collections.shuffle(serializableErasureObjects);
        FECParameterObject recobject = recserializableErasureObjects.get(0).getFecParameterObject();
        FECParameters recfecParams = FECParameters.newParameters(recobject.getDataLen(), recobject.getSymbolSize(), recobject.getNumberOfSymbols());
        final ArrayDataDecoder dec = OpenRQ.newDecoder(recfecParams, recobject.getSymbolOverhead());
        int counter = 0;
        for (SerializableErasureObject pakcet : recserializableErasureObjects) {
            final SourceBlockDecoder sbDec = dec.sourceBlock(counter);
            sbDec.putEncodingPacket(dec.parsePacket(ByteBuffer.wrap(pakcet.getOriginalPacketChunks()), false).value());
            counter++;
        }

        // compare the original and decoded data
        assertArrayEquals(data, dec.dataArray());


        TransactionBlock copys = encode.decode(data);
        assertEquals(copys, transactionBlock);
    }

    @Test
    public void SerializationWithRepairstest() throws MnemonicException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException, CloneNotSupportedException {
        BlockSizeCalculator sizeCalculator = new BlockSizeCalculator();
        sizeCalculator.setTransactionBlock(transactionBlock);
        byte[] buffer = encode.encode(transactionBlock, sizeCalculator.TransactionBlockSizeCalculator());

        long dataLen = buffer.length;
        int sizeOfCommittee = 4;
        double loss = .6;
        int numSrcBlks = sizeOfCommittee;
        int symbSize = (int) (dataLen / sizeOfCommittee);
        FECParameterObject object = FECParametersPreConditions.CalculateFECParameters(dataLen, symbSize, numSrcBlks);
        FECParameters fecParams = FECParameters.newParameters(object.getDataLen(), object.getSymbolSize(), object.getNumberOfSymbols());

        byte[] data = new byte[fecParams.dataLengthAsInt()];
        System.arraycopy(buffer, 0, data, 0, data.length);
        final ArrayDataEncoder enc = OpenRQ.newEncoder(data, fecParams);
        ArrayList<SerializableErasureObject> serializableErasureObjects = new ArrayList<SerializableErasureObject>();
        ArrayList<EncodingPacket> n = new ArrayList<EncodingPacket>();
        ArrayList<EncodingPacket> f = new ArrayList<EncodingPacket>();
        int count = 0;
        for (SourceBlockEncoder sbEnc : enc.sourceBlockIterable()) {
            for (EncodingPacket srcPacket : sbEnc.sourcePacketsIterable()) {
                n.add(srcPacket);
            }
                int numRepairSymbols = OpenRQ.minRepairSymbols(sbEnc.numberOfSourceSymbols(), object.getSymbolOverhead(), loss);
                if (numRepairSymbols > 0) {
                    for (EncodingPacket encodingPacketRepair : sbEnc.repairPacketsIterable(numRepairSymbols)) {
                        f.add(encodingPacketRepair);
                    }
                }
        }
        count = 0;
        List<List<EncodingPacket> > lists = Lists.partition(f, numSrcBlks);
        if(n.size()>f.size()) {
            for (int i = 0; i < n.size(); i++) {
                ArrayList<byte[]>bg= (ArrayList<byte[]>) lists.get(count).stream().map(EncodingPacket::asArray).collect(Collectors.toList());
                serializableErasureObjects.add(new SerializableErasureObject(object, n.get(i).asArray(), bg));
                count++;
                if (count == f.size() - 1)
                    count = 0;
            }
        }
        else{
            int finalCount = count;
            lists.stream().forEach(lst->{
                ArrayList<byte[]>repairs= (ArrayList<byte[]>) lst.stream().map(EncodingPacket::asArray).collect(Collectors.toList());
                serializableErasureObjects.add(new SerializableErasureObject(object, n.get(finalCount).asArray(), repairs));
            });
            count++;
            if (count == n.size() - 1)
                count = 0;
        }
        ArrayList<byte[]> toSend = new ArrayList<>();
        for (SerializableErasureObject obj : serializableErasureObjects) {
            toSend.add(serenc_erasure.encode(obj));
        }
        //#########################################################################################################################
        ArrayList<SerializableErasureObject> recserializableErasureObjects = new ArrayList<SerializableErasureObject>();
        for (byte[] rec_buff : toSend) {
            recserializableErasureObjects.add(serenc_erasure.decode(rec_buff));
        }
        assertEquals(serializableErasureObjects, recserializableErasureObjects);

        Collections.shuffle(serializableErasureObjects);
        FECParameterObject recobject = recserializableErasureObjects.get(0).getFecParameterObject();
        FECParameters recfecParams = FECParameters.newParameters(recobject.getDataLen(), recobject.getSymbolSize(), recobject.getNumberOfSymbols());
        final ArrayDataDecoder dec = OpenRQ.newDecoder(recfecParams, recobject.getSymbolOverhead());

        for(int i=0;i<recserializableErasureObjects.size()/2;i++){
            EncodingPacket encodingPacket=dec.parsePacket(ByteBuffer.wrap(recserializableErasureObjects.get(i).getOriginalPacketChunks()),false).value();
            final SourceBlockDecoder sbDec = dec.sourceBlock(encodingPacket.sourceBlockNumber());
            sbDec.putEncodingPacket(encodingPacket);
        }
        ArrayList<EncodingPacket> rec_f = new ArrayList<EncodingPacket>();
        for(SerializableErasureObject obj:recserializableErasureObjects){
            obj.getRepairPacketChunks().stream().forEach(val->rec_f.add(dec.parsePacket(val,false).value()));
        }
        for(int i=0;i<rec_f.size();i++){
            final SourceBlockDecoder sbDec = dec.sourceBlock(rec_f.get(i).sourceBlockNumber());
            sbDec.putEncodingPacket(rec_f.get(i));
            boolean val = Arrays.equals(data, dec.dataArray());
            if (val) {
                break;
            }
        }

        // compare the original and decoded data
        assertArrayEquals(data, dec.dataArray());

        TransactionBlock copys = encode.decode(data);
        assertEquals(copys, transactionBlock);
    }

    @Test
    public void SerializationWithRepairsLooptest() throws MnemonicException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException, CloneNotSupportedException {
        BlockSizeCalculator sizeCalculator = new BlockSizeCalculator();
        sizeCalculator.setTransactionBlock(transactionBlock);
        byte[] buffer = encode.encode(transactionBlock, sizeCalculator.TransactionBlockSizeCalculator());

        long dataLen = buffer.length;
        int sizeOfCommittee = 4;
        while (sizeOfCommittee < 200) {
            double loss = .6;
            int numSrcBlks = sizeOfCommittee;
            int symbSize = (int) (dataLen / sizeOfCommittee);
            FECParameterObject object = FECParametersPreConditions.CalculateFECParameters(dataLen, symbSize, numSrcBlks);
            FECParameters fecParams = FECParameters.newParameters(object.getDataLen(), object.getSymbolSize(), object.getNumberOfSymbols());

            byte[] data = new byte[fecParams.dataLengthAsInt()];
            System.arraycopy(buffer, 0, data, 0, data.length);
            final ArrayDataEncoder enc = OpenRQ.newEncoder(data, fecParams);
            ArrayList<SerializableErasureObject> serializableErasureObjects = new ArrayList<SerializableErasureObject>();
            ArrayList<EncodingPacket> n = new ArrayList<EncodingPacket>();
            ArrayList<EncodingPacket> f = new ArrayList<EncodingPacket>();
            int count = 0;
            for (SourceBlockEncoder sbEnc : enc.sourceBlockIterable()) {
                for (EncodingPacket srcPacket : sbEnc.sourcePacketsIterable()) {
                    n.add(srcPacket);
                }
                int numRepairSymbols = OpenRQ.minRepairSymbols(sbEnc.numberOfSourceSymbols(), object.getSymbolOverhead(), loss);
                if (numRepairSymbols > 0) {
                    for (EncodingPacket encodingPacketRepair : sbEnc.repairPacketsIterable(numRepairSymbols)) {
                        f.add(encodingPacketRepair);
                    }
                }
            }
            count = 0;
            List<List<EncodingPacket>> lists = Lists.partition(f, numSrcBlks);
            if (n.size() > f.size()) {
                for (int i = 0; i < n.size(); i++) {
                    ArrayList<byte[]> bg = (ArrayList<byte[]>) lists.get(count).stream().map(EncodingPacket::asArray).collect(Collectors.toList());
                    serializableErasureObjects.add(new SerializableErasureObject(object, n.get(i).asArray(), bg));
                    count++;
                    if (count == f.size() - 1)
                        count = 0;
                }
            } else {
                int finalCount = count;
                lists.stream().forEach(lst -> {
                    ArrayList<byte[]> repairs = (ArrayList<byte[]>) lst.stream().map(EncodingPacket::asArray).collect(Collectors.toList());
                    serializableErasureObjects.add(new SerializableErasureObject(object, n.get(finalCount).asArray(), repairs));
                });
                count++;
                if (count == n.size() - 1)
                    count = 0;
            }
            ArrayList<byte[]> toSend = new ArrayList<>();
            for (SerializableErasureObject obj : serializableErasureObjects) {
                toSend.add(serenc_erasure.encode(obj));
            }
            //#########################################################################################################################
            ArrayList<SerializableErasureObject> recserializableErasureObjects = new ArrayList<SerializableErasureObject>();
            for (byte[] rec_buff : toSend) {
                recserializableErasureObjects.add(serenc_erasure.decode(rec_buff));
            }
            assertEquals(serializableErasureObjects, recserializableErasureObjects);

            Collections.shuffle(serializableErasureObjects);
            FECParameterObject recobject = recserializableErasureObjects.get(0).getFecParameterObject();
            FECParameters recfecParams = FECParameters.newParameters(recobject.getDataLen(), recobject.getSymbolSize(), recobject.getNumberOfSymbols());
            final ArrayDataDecoder dec = OpenRQ.newDecoder(recfecParams, recobject.getSymbolOverhead());

            for (int i = 0; i < recserializableErasureObjects.size() / 2; i++) {
                EncodingPacket encodingPacket = dec.parsePacket(ByteBuffer.wrap(recserializableErasureObjects.get(i).getOriginalPacketChunks()), false).value();
                final SourceBlockDecoder sbDec = dec.sourceBlock(encodingPacket.sourceBlockNumber());
                sbDec.putEncodingPacket(encodingPacket);
            }
            ArrayList<EncodingPacket> rec_f = new ArrayList<EncodingPacket>();
            for (SerializableErasureObject obj : recserializableErasureObjects) {
                obj.getRepairPacketChunks().stream().forEach(val -> rec_f.add(dec.parsePacket(val, false).value()));
            }
            for (int i = 0; i < rec_f.size(); i++) {
                final SourceBlockDecoder sbDec = dec.sourceBlock(rec_f.get(i).sourceBlockNumber());
                sbDec.putEncodingPacket(rec_f.get(i));
                boolean val = Arrays.equals(data, dec.dataArray());
                if (val) {
                    break;
                }
            }

            // compare the original and decoded data
            assertArrayEquals(data, dec.dataArray());

            TransactionBlock copys = encode.decode(data);
            assertEquals(copys, transactionBlock);

            sizeOfCommittee++;
        }
    }
}
