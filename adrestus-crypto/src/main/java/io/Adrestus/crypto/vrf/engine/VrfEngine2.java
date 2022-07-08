package io.Adrestus.crypto.vrf.engine;

import io.Adrestus.crypto.HashUtil;
import io.Adrestus.crypto.bls.constants.Constants;
import io.Adrestus.crypto.bls.model.Params;
import org.apache.commons.codec.binary.Hex;
import org.apache.milagro.amcl.BLS381.BIG;
import org.apache.milagro.amcl.BLS381.ROM;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.field.FiniteField;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import org.apache.milagro.amcl.BLS381.ECP;
public class VrfEngine2 {
    private static final int fpPointSize = BIG.MODBYTES;
    private String curveName;
    private ECParameterSpec curveParams;
    private int qlen;
    private BigInteger order;
    private int n;
    private byte suiteString;
    private byte cofactor;
    private static long[] wr = new long[7];
    private Params params;
    public VrfEngine2(String curveName, Params params) throws Exception {
        this.params=params;
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



    private ECP arbitraryStringToPoint(byte[] data) throws Exception {
        return mapit(data);
    }

    private ECP hashToTryAndIncrement(ECP publicKey, byte[] alpha) throws Exception {

        byte[] pkBytes = new byte[fpPointSize + 1];
        publicKey.toBytes(pkBytes,true);

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
                e.printStackTrace();
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


    public static BIG mapbytes(byte[] b, int n) {
        BIG m = new BIG(0);

        for(int i = 0; i < b.length; ++i) {
            m.fshl(8);
            long[] var10000 = wr;
            var10000[0] += (long)(b[i + n] & 255);
        }

        return m;
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

    private BigInteger hashPoints(ECP[] points) throws Exception {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(suiteString);
        bos.write(2);

        for(ECP point : points) {
            byte[] bufs = new byte[fpPointSize + 1];
            point.toBytes(bufs,true);
            bos.write(bufs);
        }

        byte[] hash = HashUtil.sha256(bos.toByteArray());
        byte[] hashTrunc = new byte[this.n/8];
        System.arraycopy(hash, 0, hashTrunc, 0, hashTrunc.length);

        return new BigInteger(1, hashTrunc);
    }

    public byte[] prove(byte[] secretKey, byte[] alpha) throws Exception {

        BigInteger secretKeyBigNum  = new BigInteger(1, secretKey);

        ECP publicKeyPoint = params.g.value.mul(frombytearrayto_big(secretKey,0));
        ECP hPoint  = hashToTryAndIncrement(publicKeyPoint, alpha);
        if(hPoint == null) return null;

        byte[] hString = new byte[fpPointSize + 1];
        hPoint.toBytes(hString,true);
        ECP gammaPoint  = hPoint.mul(frombytearrayto_big(secretKey,0));

        BigInteger k = generateNonce(secretKey, hString);
        ECP uPoint = params.g.value.mul(frombytearrayto_big(k.toByteArray(),0));
        ECP vPoint = hPoint.mul(frombytearrayto_big(k.toByteArray(),0));
        BigInteger c = hashPoints(new ECP[] {hPoint, gammaPoint, uPoint,vPoint});

        BigInteger s = k.add(c.multiply(secretKeyBigNum)).mod(order);
        byte[] gammaString  = new byte[fpPointSize + 1];
        gammaPoint.toBytes(gammaString,true);
        byte[] cString = appendLeadingZeros(getBigIntegerBytes(c), this.n);
        byte[] sString = appendLeadingZeros(getBigIntegerBytes(s), this.qlen);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(gammaString);
        bos.write(cString);
        bos.write(sString);
        return bos.toByteArray();
    }

    private byte[] gammaToHash(ECP gamma) throws Exception {
        ECP gammaCof = gamma.mul(frombytearrayto_big(new BigInteger(1, new byte[] {cofactor}).toByteArray(),0));
        byte[] gammaString =  new byte[fpPointSize + 1];
        gammaCof.toBytes(gammaString,true);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(suiteString);
        bos.write(3);
        bos.write(gammaString);

        return HashUtil.sha256(bos.toByteArray());
    }

    public byte[] proofToHash(byte[] pi) throws Exception {
        Object[] objs = decodeProof(pi);
        ECP gammaPoint = (ECP) objs[0];
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
        ECP gamma = mapit(gammaBytes);

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
        BigInteger secretKeyBigNum  = new BigInteger(1, y);
        ECP publicKeyPoint = params.g.value.mul(frombytearrayto_big(y,0));
        Object[] objs = decodeProof(pi);
        ECP gammaPoint = (ECP) objs[0];
        BigInteger c = (BigInteger) objs[1];
        BigInteger s = (BigInteger) objs[2];

        //ECP publicKeyPoint  = ECP.mapit(y);
        ECP hPoint = hashToTryAndIncrement(publicKeyPoint, alpha);

        ECP sb = params.g.value.mul(frombytearrayto_big(s.toByteArray(),0));
        ECP cy = publicKeyPoint.mul(frombytearrayto_big(c.toByteArray(),0));
        cy.neg();
        sb.add(cy);

        ECP sh = hPoint.mul(frombytearrayto_big(s.toByteArray(),0));
        ECP cGamma = gammaPoint.mul(frombytearrayto_big(c.toByteArray(),0));
        cGamma.neg();

        sh.add(cGamma);
        BigInteger derivedC  = hashPoints(new ECP[] {hPoint, gammaPoint, sb,sh});

        if(derivedC.compareTo(c) != 0) {
            return null;
        }

        return gammaToHash(gammaPoint);

    }

    public static String gethexfrombytes(ECP ecp){
        byte[] buf = new byte[Constants.GROUP_G1_SIZE];
        ecp.toBytes(buf,true);
        return Hex.encodeHexString(buf);
    }
    public static String gethexfrombytes(BIG big){
        byte[] buf = new byte[Constants.GROUP_G1_SIZE];
        big.toBytes(buf);
        return Hex.encodeHexString(buf);
    }

    public static ECP mapit(byte[] h) {
        BIG q = new BIG(ROM.Modulus);
        BIG x = frombytearrayto_big(h,0);
        x.mod(q);

        ECP P;
        do {
            do {
                P = new ECP(x, 0);
                x.inc(1);
                x.norm();
            } while(P.is_infinity());

            P.cfp();
        } while(P.is_infinity());

        return P;
    }
    public  static BIG frombytearrayto_big(byte[] b, int n) {
        BIG m=new BIG(0);

        for (int i=0;i< b.length;i++)
        {
            m.fshl(8);
            wr[0]+=(int)b[i+n]&0xff;
            //m.inc((int)b[i]&0xff);
        }
        return new BIG(wr);
    }
}

