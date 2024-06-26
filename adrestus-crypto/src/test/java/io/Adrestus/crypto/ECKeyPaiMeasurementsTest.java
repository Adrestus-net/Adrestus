package io.Adrestus.crypto;

import io.Adrestus.config.AdrestusConfiguration;
import io.Adrestus.crypto.elliptic.*;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.spongycastle.util.encoders.Hex;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
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

    @Setup(Level.Trial)
    public static void setup() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        String mnemonic_code = "fd8cee9c1a3f3f57ab51b25740b24341ae093c8f697fde4df948050d3acd1700f6379d716104d2159e4912509c40ac81714d833e93b822e5ba0fadd68d5568a2";
        SecureRandom random = SecureRandom.getInstance(AdrestusConfiguration.ALGORITHM, AdrestusConfiguration.PROVIDER);
        random.setSeed(Hex.decode(mnemonic_code));

        ecKeyPair = Keys.createEcKeyPair(random);
        ecdsaSign = new ECDSASign();


        hash = message.getBytes(StandardCharsets.UTF_8);
        //hash = HashUtil.sha256(message.getBytes());

        signatureData = ecdsaSign.secp256SignMessage(message.getBytes(), ecKeyPair);
        ecKeyPair2 = Keys.createEcKeyPair(random);

        signature = ecKeyPair2.sign(message.getBytes());
    }

    @Benchmark
    @Threads(16)
    @Fork(jvmArgsAppend = {"-XX:+UseZGC"})
    public static void ECDSA() {
        boolean verify = ecdsaSign.secp256Verify(hash, ecKeyPair.getPublicKey(), signatureData);
        assertEquals(verify, true);
    }


    // @Threads(1)
    // @Benchmark
    public static void ECCDSA2() {
        boolean verify = ecKeyPair2.verify(message.getBytes(), signature);

        assertEquals(verify, true);
    }


    @Test
    public void Test() throws RunnerException {
        final Options options = new OptionsBuilder()
                .include(ECKeyPaiMeasurementsTest.class.getSimpleName())
                .measurementIterations(3)
                .forks(0)
                .warmupIterations(5)
                .build();

        new Runner(options).run();
    }

   /* Benchmark                             Mode  Cnt     Score    Error  Units
    Adrestus.crypto.Example.App.ECCDSA2  thrpt   10  4122.761 ± 74.234  ops/s
    Adrestus.crypto.Example.App.ECDSA    thrpt   10  5426.250 ± 77.091  ops/s*/
}
