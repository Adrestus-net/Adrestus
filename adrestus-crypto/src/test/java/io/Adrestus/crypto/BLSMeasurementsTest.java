package io.Adrestus.crypto;

import io.Adrestus.crypto.bls.model.*;
import io.Adrestus.crypto.bls.utils.MultiSigFastUtils;
import org.apache.tuweni.bytes.Bytes;
import org.junit.jupiter.api.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;

@BenchmarkMode({Mode.AverageTime})
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class BLSMeasurementsTest {

    @Benchmark
    public void multi_sig_fast() {
        byte[] msg = "Test_Message".getBytes();
        BLSPrivateKey sk1 = new BLSPrivateKey(new SecureRandom());
        BLSPublicKey vk1 = new BLSPublicKey(sk1);

        BLSPrivateKey sk2 = new BLSPrivateKey(new SecureRandom());
        BLSPublicKey vk2 = new BLSPublicKey(sk2);

        BLSKeyPair keypair1 = new BLSKeyPair(sk1, vk1);
        BLSKeyPair keypair2 = new BLSKeyPair(sk2, vk2);

        Signature sig1 = BLSSignature.sign(msg, keypair1.getPrivateKey());
        Signature sig2 = BLSSignature.sign(msg, keypair2.getPrivateKey());
        Signature sig = MultiSigFastUtils.mergeSignature(new Signature[]{sig1, sig2}, new BLSPublicKey[]{keypair1.getPublicKey(), keypair2.getPublicKey()});
        assertEquals(true, MultiSigFastUtils.verify(sig, msg, new BLSPublicKey[]{keypair1.getPublicKey(), keypair2.getPublicKey()}));
    }

    @Benchmark
    public void fastAggregateVerify1(){
        Bytes message = Bytes.wrap("Hello, world 1!".getBytes(UTF_8));
        BLSPrivateKey sk1 = new BLSPrivateKey(new SecureRandom());
        BLSPublicKey vk1 = new BLSPublicKey(sk1);

        BLSPrivateKey sk2 = new BLSPrivateKey(new SecureRandom());
        BLSPublicKey vk2 = new BLSPublicKey(sk2);

        BLSPrivateKey sk3 = new BLSPrivateKey(new SecureRandom());
        BLSPublicKey vk3 = new BLSPublicKey(sk3);

        BLSKeyPair keyPair1 = new BLSKeyPair(sk1, vk1);
        BLSKeyPair keyPair2 = new BLSKeyPair(sk2, vk2);
        BLSKeyPair keyPair3 = new BLSKeyPair(sk3, vk3);
        List<BLSPublicKey> publicKeys = Arrays.asList(keyPair1.getPublicKey(), keyPair2.getPublicKey(), keyPair3.getPublicKey());
        List<Signature> signatures = Arrays.asList(BLSSignature.sign(message.toArray(),keyPair1.getPrivateKey()), BLSSignature.sign(message.toArray(),keyPair2.getPrivateKey()), BLSSignature.sign(message.toArray(),keyPair3.getPrivateKey()));
        Signature aggregatedSignature = BLSSignature.aggregate(signatures);
        assertEquals(true,BLSSignature.fastAggregateVerify(publicKeys, message, aggregatedSignature));
    }

    @Test
    public void maintest() throws RunnerException {
    /* final Options options = new OptionsBuilder()
                .include(BLSMeasurementsTest.class.getSimpleName())
                .measurementIterations(10)
                .forks(0)
                .warmupIterations(1)
                .build();

        new Runner(options).run();*/
    }
}
