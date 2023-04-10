package io.Adrestus.core;

import io.Adrestus.config.AdrestusConfiguration;
import io.Adrestus.crypto.HashUtil;
import io.Adrestus.crypto.WalletAddress;
import io.Adrestus.crypto.elliptic.ECDSASign;
import io.Adrestus.crypto.elliptic.ECDSASignatureData;
import io.Adrestus.crypto.elliptic.ECKeyPair;
import io.Adrestus.crypto.elliptic.Keys;
import io.Adrestus.crypto.elliptic.mapper.BigIntegerSerializer;
import io.Adrestus.crypto.elliptic.mapper.SignatureDataSerializer;
import io.Adrestus.crypto.mnemonic.Mnemonic;
import io.Adrestus.crypto.mnemonic.Security;
import io.Adrestus.crypto.mnemonic.WordList;
import io.Adrestus.util.GetTime;
import io.Adrestus.util.SerializationUtil;
import io.activej.serializer.BinarySerializer;
import io.activej.serializer.SerializerBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SignatureSerializationTest {
    static ArrayList<String> addreses = new ArrayList<>();
    static ArrayList<ECKeyPair> keypair = new ArrayList<>();
    static int start = 0;
    static int end = 10000;
    static BinarySerializer<Transaction> enc = SerializerBuilder
            .create()
            .with(ECDSASignatureData.class, ctx -> new SignatureDataSerializer())
            .with(BigInteger.class, ctx -> new BigIntegerSerializer())
            .build(Transaction.class);
    static SerializationUtil<Transaction> serenc;
    static ECDSASign ecdsaSign = new ECDSASign();

    @BeforeAll
    public static void setup() throws Exception {
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        list.add(new SerializationUtil.Mapping(ECDSASignatureData.class, ctx -> new SignatureDataSerializer()));
        serenc = new SerializationUtil<Transaction>(Transaction.class, list);
        int version = 0x00;
        for (int i = start; i < end; i++) {
            Mnemonic mnem = new Mnemonic(Security.NORMAL, WordList.ENGLISH);
            char[] mnemonic_sequence = "sample sail jungle learn general promote task puppy own conduct green affair ".toCharArray();
            char[] passphrase = ("p4ssphr4se" + String.valueOf(i)).toCharArray();
            byte[] key = mnem.createSeed(mnemonic_sequence, passphrase);
            SecureRandom randoms = SecureRandom.getInstance(AdrestusConfiguration.ALGORITHM, AdrestusConfiguration.PROVIDER);
            randoms.setSeed(key);
            ECKeyPair ecKeyPair = Keys.createEcKeyPair(randoms);
            String adddress = WalletAddress.generate_address((byte) version, ecKeyPair.getPublicKey());
            addreses.add(adddress);
            keypair.add(ecKeyPair);
        }
    }

    @Test
    public void test() {

        ECDSASignatureData s = new ECDSASignatureData("s".getBytes(StandardCharsets.UTF_8)[0], "asdas".getBytes(StandardCharsets.UTF_8), "Sad".getBytes(StandardCharsets.UTF_8), "asdasa".getBytes(StandardCharsets.UTF_8));
        Transaction tr = new RegularTransaction("hash1");
        tr.setSignature(s);

        byte[] buffer = new byte[1024];
        enc.encode(buffer, 0, tr);
        Transaction copy = enc.decode(buffer, 0);
        assertEquals(tr, copy);
    }

    @Test
    public void test2() {
        for (int i = start; i < end - 1; i++) {
            Transaction transaction = new RegularTransaction();
            transaction.setFrom(addreses.get(i));
            transaction.setTo(addreses.get(i + 1));
            transaction.setStatus(StatusType.PENDING);
            transaction.setTimestamp(GetTime.GetTimeStampInString());
            transaction.setZoneFrom(0);
            transaction.setZoneTo(0);
            transaction.setAmount(100);
            transaction.setAmountWithTransactionFee(transaction.getAmount() * (10.0 / 100.0));
            transaction.setNonce(1);

            byte byf[] = serenc.encode(transaction, 1024);
            transaction.setHash(HashUtil.sha256_bytetoString(byf));

            ECDSASignatureData signatureData = ecdsaSign.secp256SignMessage(Hex.decode(transaction.getHash()), keypair.get(i));
            transaction.setSignature(signatureData);

            byte byf2[] = serenc.encode(transaction, 1024);
            Transaction clone = serenc.decode(byf2);
            assertEquals(transaction, clone);

        }
    }

}
