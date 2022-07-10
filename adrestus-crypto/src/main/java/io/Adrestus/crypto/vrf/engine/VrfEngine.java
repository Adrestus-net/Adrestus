package io.Adrestus.crypto.vrf.engine;

import io.Adrestus.crypto.HashUtil;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.field.FiniteField;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;

public class VrfEngine {

    private String curveName;
    private ECParameterSpec curveParams;
    private int qlen;
    private BigInteger order;
    private int n;
    private byte suiteString;
    private byte cofactor;
    private ECCurve CURVES;
    public VrfEngine(String curveName) throws Exception {

        this.curveName = curveName;
        this.curveParams = ECNamedCurveTable.getParameterSpec(curveName);

        if (curveParams == null)
            throw new IllegalArgumentException("Unsupported curve: " + curveName);

        FiniteField field = curveParams.getCurve().getField();
        BigInteger p = field.getCharacteristic();

        this.n = ((p.bitLength() + (p.bitLength() % 2)) / 2);
        this.order = curveParams.getCurve().getOrder();
        this.qlen = order.bitLength();
        this.suiteString = getSuiteString();
        this.cofactor = getCofactor();

    }

    byte getSuiteString() {
        if("prime256v1".equals(curveName)) {
            return 1;
        }

        if("secp256k1".equals(curveName)) {
            return 1;
        }

        throw new RuntimeException("Unsupported curve: " + curveName);
    }

    byte getCofactor() {
        if("prime256v1".equals(curveName)) {
            return 1;
        }

        if("secp256k1".equals(curveName)) {
            return 1;
        }

        throw new RuntimeException("Unsupported curve: " + curveName);
    }

    public ECPoint zero() {

        return curveParams.getCurve().getInfinity();
    }

    public ECPoint decode(byte[] encoded) {

        return curveParams.getCurve().decodePoint(encoded);
    }

    public byte[] encode(ECPoint g) {

        return g.getEncoded(true);
    }

    private ECPoint derivePublicKeyPoint(BigInteger secretKeyBigNum) {

        return curveParams.getG().multiply(secretKeyBigNum);
    }

    public byte[] derivePublicKey(byte[] secret) {

        BigInteger secretKeyBigNum  = new BigInteger(1, secret);
        ECPoint point = derivePublicKeyPoint(secretKeyBigNum);
        return encode(point);
    }

    private ECPoint arbitraryStringToPoint(byte[] data) throws Exception {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(2);
        bos.write(data);
        return decode(bos.toByteArray());
    }

    private ECPoint hashToTryAndIncrement(ECPoint publicKey, byte[] alpha) throws Exception {

        byte[] pkBytes = encode(publicKey);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
       // bos.write(suiteString);
      //  bos.write(1);
        bos.write(pkBytes);
        bos.write(alpha);
       // bos.write(0);
        byte[] v = bos.toByteArray();

        int pos = v.length-1;

        for(int c = 0; c < 255; c++)
        {
            try {
                v[pos] = (byte)c;
                byte[] attemptedHash  = HashUtil.sha256(v);
                return arbitraryStringToPoint(attemptedHash);
            }catch(Exception e) {

            }

        }
        return null;
    }

    private BigInteger bits2int(byte[] data, int qlen) {

        int dataLenBits = data.length * 8;
        BigInteger dataBignum = new BigInteger(1, data);
        BigInteger result;

        if(dataLenBits > qlen) {
            result = dataBignum.shiftRight(dataLenBits - qlen);
        }else {
            result = dataBignum;
        }

        return result;
    }

    private byte[] getBigIntegerBytes(BigInteger b) {

        byte[] buf = b.toByteArray();

        if(buf[0] == 0) {
            byte[] tmp = new byte[buf.length-1];
            System.arraycopy(buf, 1, tmp, 0, tmp.length);
            return tmp;
        }
        return buf;
    }

    private byte[] bits2octets(byte[] data, int length, BigInteger order) {

        BigInteger z1 = bits2int(data, length);
        BigInteger result = z1.mod(order);
        return getBigIntegerBytes(result);
    }

    private byte[] appendLeadingZeros(byte[] data, int bitsLength) {

        if(data.length * 8> bitsLength) {
            return data;
        }

        int paddingLen;

        if(bitsLength % 8 > 0) {
            paddingLen = (bitsLength / 8 - data.length + 1);

        }else {
            paddingLen = (bitsLength / 8 - data.length);
        }

        byte[] result = new byte[paddingLen + data.length];
        System.arraycopy(data, 0, result, paddingLen, data.length);
        return result;
    }


    private BigInteger generateNonce(byte[] secretKey, byte[] data) throws Exception {

        byte[] dataHash = HashUtil.sha256(data);
        byte[] dataTrunc  = bits2octets(dataHash, qlen, order);

        byte[] paddedDataTrunc  = appendLeadingZeros(dataTrunc, qlen);
        byte[] paddedSecretKeyBytes = appendLeadingZeros(secretKey, qlen);

        byte[] v = new byte[32];
        for(int i = 0; i < v.length; i++) v[i] = 1;

        byte[] k = new byte[32];

        for(int prefix = 0; prefix < 2; prefix++) {

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bos.write(v);
            bos.write(prefix);
            bos.write(paddedSecretKeyBytes);
            bos.write(paddedDataTrunc);

            k =  HashUtil.mac(bos.toByteArray(), k);
            v =HashUtil.mac(v, k);
        }

        while(true) {
            v = HashUtil.mac(v, k);
            BigInteger result = bits2int(v, qlen);

            if(result.signum() > 0 && result.compareTo(order) < 0) {
                return result;
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bos.write(v);
            bos.write(0);
            k = HashUtil.mac(bos.toByteArray(), k);
            v = HashUtil.mac(v, k);
        }
    }

    private BigInteger hashPoints(ECPoint[] points) throws Exception {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(suiteString);
        bos.write(2);

        for(ECPoint point : points) {
            bos.write(encode(point));
        }

        byte[] hash = HashUtil.sha256(bos.toByteArray());
        byte[] hashTrunc = new byte[this.n/8];
        System.arraycopy(hash, 0, hashTrunc, 0, hashTrunc.length);

        return new BigInteger(1, hashTrunc);
    }

    public byte[] prove(byte[] secretKey, byte[] alpha) throws Exception {

        BigInteger secretKeyBigNum  = new BigInteger(1, secretKey);
        ECPoint publicKeyPoint = derivePublicKeyPoint(secretKeyBigNum);
        ECPoint hPoint  = hashToTryAndIncrement(publicKeyPoint, alpha);
        if(hPoint == null) return null;

        byte[] hString = encode(hPoint);
        ECPoint gammaPoint  = hPoint.multiply(secretKeyBigNum);

        BigInteger k = generateNonce(secretKey, hString);
        ECPoint uPoint = curveParams.getG().multiply(k);
        ECPoint vPoint = hPoint.multiply(k);
        BigInteger c = hashPoints(new ECPoint[] {hPoint, gammaPoint, uPoint, vPoint});

        BigInteger s = k.add(c.multiply(secretKeyBigNum)).mod(order);
        byte[] gammaString  = encode(gammaPoint);
        byte[] cString = appendLeadingZeros(getBigIntegerBytes(c), this.n);
        byte[] sString = appendLeadingZeros(getBigIntegerBytes(s), this.qlen);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(gammaString);
        bos.write(cString);
        bos.write(sString);

        return bos.toByteArray();
    }

    private byte[] gammaToHash(ECPoint gamma) throws Exception {
        ECPoint gammaCof = gamma.multiply(new BigInteger(1, new byte[] {cofactor}));
        byte[] gammaString =  encode(gammaCof);
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(suiteString);
        bos.write(3);
        bos.write(gammaString);
        
        return HashUtil.sha256(bos.toByteArray());
    }
    
    public byte[] proofToHash(byte[] pi) throws Exception {
        Object[] objs = decodeProof(pi);
        ECPoint gammaPoint = (ECPoint) objs[0];
        return gammaToHash(gammaPoint);          
    }
    
    private Object[] decodeProof(byte[] pi) {
        int gammaOct  =  qlen % 8 > 0 ? (qlen / 8 + 2) : (qlen / 8 + 1);
        int cOct = n%8 > 0? (n/8 + 1) : n/8;
        
        if(pi.length * 8 < gammaOct + cOct*3) {
            throw new RuntimeException("Invalid pi length");
        }
        
        byte[] gammaBytes = new byte[gammaOct];
        System.arraycopy(pi, 0, gammaBytes, 0, gammaOct);         
        ECPoint gamma = decode(gammaBytes);
        
        byte[] cBytes = new byte[cOct];
        System.arraycopy(pi, gammaOct, cBytes, 0, cOct);
        BigInteger c = new BigInteger(1, cBytes);

        byte[] sBytes = new byte[pi.length - gammaOct - cOct];
        System.arraycopy(pi, gammaOct + cOct, sBytes , 0, sBytes.length);
        BigInteger s = new BigInteger(1, sBytes);

        return new Object[] {gamma, c, s};        
        
    }
    
    public byte[] verify(byte[] y, byte[] pi, byte[] alpha) throws Exception {
        
        if(pi == null) return null;
        
        Object[] objs = decodeProof(pi);
        ECPoint gammaPoint = (ECPoint) objs[0];
        BigInteger c = (BigInteger) objs[1];
        BigInteger s = (BigInteger) objs[2];
        
        ECPoint publicKeyPoint  = decode(y);
        ECPoint hPoint = hashToTryAndIncrement(publicKeyPoint, alpha);
        
        ECPoint sb = curveParams.getG().multiply(s);
        ECPoint cy = publicKeyPoint.multiply(c);
        cy = cy.negate();
        ECPoint uPoint = sb.add(cy);
        
        ECPoint sh = hPoint.multiply(s);
        ECPoint cGamma = gammaPoint.multiply(c);
        cGamma = cGamma.negate();
        
        ECPoint vPoint = sh.add(cGamma);
        BigInteger derivedC  = hashPoints(new ECPoint[] {hPoint, gammaPoint, uPoint, vPoint});
        
        if(derivedC.compareTo(c) != 0) {
            return null;
        }
        
        return gammaToHash(gammaPoint);
        
    }
}