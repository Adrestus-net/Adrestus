package io.Adrestus.crypto.vrf.engine;

import io.Adrestus.crypto.HashUtil;
import io.Adrestus.crypto.bls.model.Params;
import io.Adrestus.crypto.bls.utils.ConvertUtil;
import org.apache.milagro.amcl.BLS381.BIG;
import org.apache.milagro.amcl.BLS381.ECP;
import org.bouncycastle.util.encoders.Hex;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;

public class VrfEngine2 {
    private static final int fpPointSize = BIG.MODBYTES;
    private static final int qlen = 128;
    public static final BigInteger Q = new BigInteger(1, Hex.decode("115792089237316195423570985008687907852837564279074904382605163141518161494337"));
    private Params params;
    public VrfEngine2(Params params) throws Exception {
        this.params=params;
    }


    private ECP arbitraryStringToPoint(byte[] data) throws Exception {
        return ConvertUtil.mapit(data);
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


    private BigInteger generateNonce(byte[] secretKey, byte[] data) throws Exception {

        byte[] dataHash = HashUtil.sha256(data);

        byte[] v = new byte[32];
        for(int i = 0; i < v.length; i++) v[i] = 1;

        byte[] k = new byte[32];

        for(int prefix = 0; prefix < 2; prefix++) {

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bos.write(v);
            bos.write(prefix);
            bos.write(secretKey);
            bos.write(dataHash);

            k =  HashUtil.mac(bos.toByteArray(), k);
            v =HashUtil.mac(v, k);
        }

        while(true) {
            v = HashUtil.mac(v, k);
            BigInteger result = bits2int(v, qlen);

            if(result.signum() > 0 && result.compareTo(Q) < 0) {
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
        bos.write(1);
        bos.write(2);

        for(ECP point : points) {
            byte[] bufs = new byte[fpPointSize + 1];
            point.toBytes(bufs,true);
            bos.write(bufs);
        }

        byte[] hash = HashUtil.sha256(bos.toByteArray());
        byte[] hashTrunc = new byte[qlen/8];
        System.arraycopy(hash, 0, hashTrunc, 0, hashTrunc.length);

        return new BigInteger(1, hashTrunc);
    }

    public byte[] prove(byte[] secretKey, byte[] alpha) throws Exception {

        BigInteger secretKeyBigNum  = new BigInteger(1, secretKey);


        ECP publicKeyPoint = params.g.value.mul(ConvertUtil.byte_to_BIG(secretKey,0));
        ECP hPoint  = hashToTryAndIncrement(publicKeyPoint, alpha);

        if(hPoint == null) return null;

        byte[] hString = ConvertUtil.ecp_to_bytes(hPoint);
        ECP gammaPoint  = hPoint.mul(ConvertUtil.byte_to_BIG(secretKey,0));

        BigInteger k = generateNonce(secretKey, hString);
        ECP uPoint = params.g.value.mul(ConvertUtil.byte_to_BIG(k.toByteArray(),0));
        ECP vPoint = hPoint.mul(ConvertUtil.byte_to_BIG(k.toByteArray(),0));

        // BigInteger c = hashPoints(new ECP[] {hPoint, gammaPoint, uPoint,vPoint});
        BigInteger c = hashPoints(new ECP[]{hPoint, gammaPoint});
        BigInteger s = k.add(c.multiply(secretKeyBigNum)).mod(Q);


        byte[] gammaString  = ConvertUtil.ecp_to_bytes(gammaPoint);
        byte[] cString = c.toByteArray();
        byte[] sString = s.toByteArray();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(gammaString);
        int gammaInt=bos.size();
        bos.write(cString);
        int cSInt=bos.size();
        bos.write(sString);
        int sSInt=bos.size();

        bos.write(gammaInt);
        bos.write(cSInt);
        bos.write(sSInt);
        bos.write(0);

        return bos.toByteArray();
    }

    private byte[] gammaToHash(ECP gamma) throws Exception {
        ECP gammaCof = gamma.mul(ConvertUtil.byte_to_BIG(new BigInteger(1, new byte[] {0}).toByteArray(),0));
        byte[] gammaString =  new byte[fpPointSize + 1];
        gammaCof.toBytes(gammaString,true);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(1);
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
        int gammaInt=(pi[pi.length-4]& 0xFF);
        int cSInt=(pi[pi.length-3]& 0xFF);
        int sSInt=(pi[pi.length-2]& 0xFF);
        byte[] gammaBytes = new byte[gammaInt];
        System.arraycopy(pi, 0, gammaBytes, 0, gammaInt);
        ECP gamma = ECP.fromBytes(gammaBytes);

        byte[] cBytes = new byte[cSInt-gammaInt];
        System.arraycopy(pi, gammaInt, cBytes, 0, cBytes.length);
        BigInteger c = new BigInteger(1, cBytes);

        byte[] sBytes = new byte[sSInt-cSInt];
        //byte[]sBytes=Arrays.copyOfRange(pi, cSInt, pi.length-4);
        System.arraycopy(pi, cSInt,sBytes , 0, sBytes.length);
        BigInteger s = new BigInteger(1, sBytes);

        return new Object[] {gamma, c,s};

    }

    public byte[] verify(byte[] y, byte[] pi, byte[] alpha) throws Exception {

        if(pi == null) return null;
        ECP publicKeyPoint = ECP.fromBytes(y);
        Object[] objs = decodeProof(pi);
        ECP gammaPoint = (ECP) objs[0];
        BigInteger c = (BigInteger) objs[1];
        BigInteger s = (BigInteger) objs[2];

        ECP hPoint = hashToTryAndIncrement(publicKeyPoint, alpha);

        ECP sb = params.g.value.mul(ConvertUtil.byte_to_BIG(s.toByteArray(),0));

        ECP cy = publicKeyPoint.mul(ConvertUtil.byte_to_BIG(c.toByteArray(),0));
        cy.neg();
        sb.add(cy);

        ECP sh = hPoint.mul(ConvertUtil.byte_to_BIG(s.toByteArray(),0));
        ECP cGamma = gammaPoint.mul(ConvertUtil.byte_to_BIG(c.toByteArray(),0));
        cGamma.neg();

        sh.add(cGamma);
        // BigInteger derivedC  = hashPoints(new ECP[] {hPoint, gammaPoint, sb,sh});
        BigInteger derivedC  = hashPoints(new ECP[] {hPoint, gammaPoint});

        if(derivedC.compareTo(c) != 0) {
            return null;
        }

        return gammaToHash(gammaPoint);

    }

}

