package io.Adrestus.crypto.bls.model;

import io.Adrestus.crypto.HashUtil;
import io.Adrestus.crypto.bls.BLS381.ECP;
import io.Adrestus.crypto.bls.BLS381.ECP2;
import io.Adrestus.crypto.bls.constants.Constants;
import io.Adrestus.crypto.bls.utils.CommonUtils;
import org.apache.tuweni.bytes.Bytes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;

public class BLSSignature {

    private static G2Point hashMessage(byte[] msg) {
        byte[] tmp = new byte[Constants.MESSAGE_DOMAIN_PREFIX.length + msg.length];
        System.arraycopy(Constants.MESSAGE_DOMAIN_PREFIX, 0, tmp, 0, Constants.MESSAGE_DOMAIN_PREFIX.length);
        System.arraycopy(msg, 0, tmp, Constants.MESSAGE_DOMAIN_PREFIX.length, msg.length);
        return new G2Point(HashUtil.Shake256(tmp));
    }

    public static Signature sign(byte[] msg, BLSPrivateKey sigKey) {
        G2Point hashPoint = hashMessage(msg);
        ECP2 ecp2 = hashPoint.getValue().mul(sigKey.getX().value);
        return new Signature(new G2Point(ecp2));
    }

    public static Signature sign(BLSPrivateKey PrivateKey, Bytes message) {
        G2Point hashInGroup2 = new G2Point(HashToCurve.hashToG2(message));
        ECP2 ecp2 = hashInGroup2.getValue().mul(PrivateKey.getX().value);
        return new Signature(new G2Point(ecp2));
    }

    public static boolean verify(Signature sig, byte[] msg, BLSPublicKey publicKey, Params params) {
        if (sig.getPoint().getValue().is_infinity()) {
            return false;
        }
        G2Point hashPoint = hashMessage(msg);
        G1Point g = new G1Point(params.g);
        g.neg();
        GTPoint gt = GTPoint.ate2Pairing(publicKey.getPoint(), hashPoint, g, sig.getPoint());
        return gt.value.isunity();
    }

    public static boolean verify(Signature sig, byte[] msg, BLSPublicKey publicKey, byte[] param) {
        if (sig.getPoint().getValue().is_infinity()) {
            return false;
        }
        G2Point hashPoint = hashMessage(msg);
        G1Point g = new G1Point(HashUtil.Shake256(param));
        g.neg();
        GTPoint gt = GTPoint.ate2Pairing(publicKey.getPoint(), hashPoint, g, sig.getPoint());
        return gt.value.isunity();
    }

    public static boolean verify(Signature sig, byte[] msg, BLSPublicKey publicKey) {
        if (sig.getPoint().getValue().is_infinity()) {
            return false;
        }
        G2Point hashPoint = hashMessage(msg);
        G1Point g = new G1Point(ECP.generator());
        g.neg();
        GTPoint gt = GTPoint.ate2Pairing(publicKey.getPoint(), hashPoint, g, sig.getPoint());
        return gt.value.isunity();
    }

    public static boolean verify(Signature sig, BLSPublicKey publicKey, G2Point hashInG2) {
        try {
            G1Point g = new G1Point(ECP.generator());
            g.neg();
            GTPoint e = GTPoint.pair2(publicKey.getPoint(), hashInG2, g, sig.getSupplier_point().get());
            return e.value.isunity();
        } catch (RuntimeException e) {
            return false;
        }
    }

    public static Signature aggregate(List<Signature> signatures) {
        return Signature.aggregate(signatures);
    }

    public static Signature aggregate(Stream<Signature> signatures) {
        ArrayList<Signature> cloned_keys = new ArrayList<Signature>();
        signatures.forEach(x -> cloned_keys.add((Signature) x.clone()));
        return cloned_keys.stream().reduce(Signature::combine).orElseGet(() -> new Signature(new G2Point()));
    }


    public static boolean aggregateVerify(List<BLSPublicKey> BLSPublicKeys, List<Bytes> messages, Signature signature) {
        Set<Bytes> set = new HashSet<>();
        for (Bytes message : messages) {
            if (!set.add(message)) return false;
        }
        List<G2Point> hashesInG2 = messages.stream().map(m -> new G2Point(HashToCurve.hashToG2(m))).collect(Collectors.toList());

        checkArgument(BLSPublicKeys.size() == hashesInG2.size(), "List of public keys and list of messages differ in length");
        checkArgument(BLSPublicKeys.size() > 0, "List of public keys is empty");
        try {
            GTPoint gt1 = GTPoint.pair(BLSPublicKeys.get(0).getPoint(), hashesInG2.get(0));
            for (int i = 1; i < BLSPublicKeys.size(); i++) {
                gt1 = gt1.mul(GTPoint.pair(BLSPublicKeys.get(i).getPoint(), hashesInG2.get(i)));
            }
            GTPoint gt2 = GTPoint.pair(new G1Point(ECP.generator()), signature.getPoint());
            return gt2.equals(gt1);
        } catch (RuntimeException e) {
            return false;
        }
    }

    public static boolean fastAggregateVerify(List<BLSPublicKey> BLSPublicKeys, Bytes message, Signature signature) {
        BLSPublicKey aggregated_pub = BLSPublicKey.aggregate(BLSPublicKeys);

        return BLSSignature.verify(signature, message.toArray(), aggregated_pub);
    }


    public static boolean completeBatchVerify(List<BatchSemiAggregate> preparedList) {
        if (preparedList.isEmpty()) {
            return true;
        }
        G2Point sigSum = null;
        GTPoint pairProd = null;
        for (BatchSemiAggregate semiSig : preparedList) {
            sigSum = sigSum == null ? semiSig.getSigPoint() : sigSum.add2(semiSig.getSigPoint());
            pairProd =
                    pairProd == null
                            ? semiSig.getMsgPubKeyPairing()
                            : pairProd.mul(semiSig.getMsgPubKeyPairing());
        }
        GTPoint sigPair = GTPoint.pairNoExp(new G1Point(ECP.generator()), sigSum);
        return GTPoint.fexp(sigPair).equals(GTPoint.fexp(pairProd));
    }


    public static BatchSemiAggregate prepareBatchVerify(
            int index, List<BLSPublicKey> BLSPublicKeys, byte[] message, Signature signature) {

        G2Point sigG2Point;
        G2Point msgG2Point;
        if (index == 0) {
            // optimization: we may omit multiplication of a single component (i.e. multiplier is 1)
            // let it be the component with index 0
            sigG2Point = signature.getPoint();
            msgG2Point = new G2Point(HashToCurve.hashToG2(Bytes.wrap(message)));
        } else {
            FieldElement randomMult = CommonUtils.nextBatchRandomMultiplier();
            sigG2Point = signature.getPoint().mul(randomMult);
            msgG2Point = new G2Point(HashToCurve.hashToG2(Bytes.wrap(message)).mul(randomMult.value));
        }

        GTPoint pair = GTPoint.pairNoExp(BLSPublicKey.aggregate(BLSPublicKeys).getPoint(), msgG2Point);

        return new BatchSemiAggregate(sigG2Point, pair);
    }


    public static BatchSemiAggregate prepareBatchVerify2(
            int index,
            List<BLSPublicKey> BLSPublicKeys1,
            byte[] message1,
            Signature signature1,
            List<BLSPublicKey> BLSPublicKeys2,
            byte[] message2,
            Signature signature2) {

        G2Point sigG2Point1;
        G2Point msgG2Point1;
        if (index == 0) {
            // optimization: we may omit multiplication of a single component (i.e. multiplier is 1)
            // let it be the component with index 0
            sigG2Point1 = signature1.getPoint();
            msgG2Point1 = new G2Point(HashToCurve.hashToG2(Bytes.wrap(message1)));
        } else {
            FieldElement randomMult = CommonUtils.nextBatchRandomMultiplier();
            sigG2Point1 = signature1.getPoint().mul(randomMult);
            msgG2Point1 = new G2Point(HashToCurve.hashToG2(Bytes.wrap(message1)).mul(randomMult.value));
        }
        BLSPublicKey BLSPublicKey1 = BLSPublicKey.aggregate(BLSPublicKeys1);

        FieldElement randomMult2 = CommonUtils.nextBatchRandomMultiplier();
        G2Point sigG2Point2 = signature2.getPoint().mul(randomMult2);
        G2Point msgG2Point2 = new G2Point(HashToCurve.hashToG2(Bytes.wrap(message2)).mul(randomMult2.value));
        BLSPublicKey BLSPublicKey2 = BLSPublicKey.aggregate(BLSPublicKeys2);

        GTPoint pair2 =
                GTPoint.pair2NoExp(BLSPublicKey1.getPoint(), msgG2Point1, BLSPublicKey2.getPoint(), msgG2Point2);

        return new BatchSemiAggregate(sigG2Point1.add2(sigG2Point2), pair2);
    }
}
