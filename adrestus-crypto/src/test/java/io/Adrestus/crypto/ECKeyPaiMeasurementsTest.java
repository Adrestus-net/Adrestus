package io.Adrestus.crypto;

import io.Adrestus.config.AdrestusConfiguration;
import io.Adrestus.crypto.bls.model.BLSPrivateKey;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.bls.model.BLSSignature;
import io.Adrestus.crypto.elliptic.*;
import org.conscrypt.Conscrypt;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.spongycastle.util.encoders.Hex;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@BenchmarkMode({Mode.Throughput})
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Thread)
public class ECKeyPaiMeasurementsTest {
    private static byte[] hash;
    private static ECKeyPair ecKeyPair;
    private static ECDSASign ecdsaSign;
    private static ECDSASignatureData signatureData;
    private static String message = "verify test";
    private static ECKeyPair ecKeyPair2;
    private static ECDSASignature signature;
    private static byte[] signature2;
    private static KeyPair keyPair;
    private static Signature ecdsaVerify;
    private static io.Adrestus.crypto.bls.model.Signature bls_sig;
    private static BLSPublicKey vk;

    static {
        Security.addProvider(Conscrypt.newProvider());
        Security.insertProviderAt(Conscrypt.newProvider(), 0);
    }

    @Setup(Level.Trial)
    public static void setup() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, SignatureException {
        String mnemonic_code = "fd8cee9c1a3f3f57ab51b25740b24341ae093c8f697fde4df948050d3acd1700f6379d716104d2159e4912509c40ac81714d833e93b822e5ba0fadd68d5568a2";
        SecureRandom random = SecureRandom.getInstance(AdrestusConfiguration.ALGORITHM, AdrestusConfiguration.PROVIDER);
        random.setSeed(Hex.decode(mnemonic_code));

        //Bls Test speed//////////////////////////////////////////
        BLSPrivateKey sk = new BLSPrivateKey(42);
        vk = new BLSPublicKey(sk);

        bls_sig = BLSSignature.sign(message.getBytes(StandardCharsets.UTF_8), sk);
        //Bls Test speed//////////////////////////////////////////
        //Adrestus Implementation//////////////////////////////////////////
        ecKeyPair = Keys.createEcKeyPair(random);
        ecdsaSign = new ECDSASign();


        hash = message.getBytes(StandardCharsets.UTF_8);
        //hash = HashUtil.sha256(message.getBytes());

        signatureData = ecdsaSign.secp256SignMessage(message.getBytes(), ecKeyPair);
        ecKeyPair2 = Keys.createEcKeyPair(random);

        signature = ecKeyPair2.sign(message.getBytes());
        //Adrestus Implementation//////////////////////////////////////////


        //ConscryptImplementation//////////////////////////////////////////
        // Generate key pair
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC", "Conscrypt");
        keyGen.initialize(new ECGenParameterSpec("secp256r1"));
        keyPair = keyGen.generateKeyPair();

        // Sign a message
        Signature ecdsaSign = Signature.getInstance("SHA256withECDSA", "Conscrypt");
        ecdsaSign.initSign(keyPair.getPrivate());
        ecdsaSign.update(message.getBytes());
        signature2 = ecdsaSign.sign();

        ecdsaVerify = Signature.getInstance("SHA256withECDSA", "Conscrypt");
        //ConscryptImplementation//////////////////////////////////////////

    }

    @Benchmark
    public static void BLS() {
        assertEquals(true, BLSSignature.verify(bls_sig, message.getBytes(StandardCharsets.UTF_8), vk));
    }

    @Threads(24)
    @Benchmark
    public static void ECDSA() {
        boolean verify = ecdsaSign.secp256Verify(hash, ecKeyPair.getPublicKey(), signatureData);
        assertEquals(true, verify);
    }

    @Threads(24)
    @Benchmark
    public static void Conscrypt() throws SignatureException, InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException {
        ecdsaVerify.initVerify(keyPair.getPublic());
        ecdsaVerify.update(message.getBytes());
        boolean isVerified = ecdsaVerify.verify(signature2);
    }


    @Test
    public void Test() throws RunnerException {
        final Options options = new OptionsBuilder()
                .include(ECKeyPaiMeasurementsTest.class.getSimpleName())
                .measurementIterations(3)
                .forks(0)
                .warmupIterations(3)
                .build();

        new Runner(options).run();
    }

    //24 threads
//    Benchmark                                            Mode  Cnt       Score        Error  Units
//    Adrestus.crypto.ECKeyPaiMeasurementsTest.Conscrypt  thrpt    3  248402.585 ± 119726.471  ops/s
//    Adrestus.crypto.ECKeyPaiMeasurementsTest.ECDSA      thrpt    3  143775.886 ±   5992.509  ops/s
}
