package io.Adrestus.crypto.vrf.engine;

import io.Adrestus.crypto.HashUtil;
import io.Adrestus.crypto.bls.BLS381.BIG;
import io.Adrestus.crypto.bls.BLS381.ECP;
import io.Adrestus.crypto.bls.constants.Constants;
import io.Adrestus.crypto.bls.utils.ConvertUtil;
import org.apache.milagro.amcl.BLS381.ROM;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;


public class VrfEngine2 {
    public static final BIG order = new BIG(ROM.CURVE_Order);
    
    public VrfEngine2() {
    }


    private ECP arbitraryStringToPoint(byte[] data) {
        return ECP.mapitUnlimited(data);
    }

    private ECP hashToTryAndIncrement(ECP publicKey, byte[] alpha) throws Exception {

        byte[] pkBytes = ConvertUtil.parseECPByte(publicKey);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(pkBytes);
        bos.write(alpha);
        byte[] v = bos.toByteArray();

        int pos = v.length - 1;

        for (int c = 0; c < 255; c++) {
            try {
                v[pos] = (byte) c;
                byte[] attemptedHash = HashUtil.sha256(v);
                return arbitraryStringToPoint(attemptedHash);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return null;
    }

    private BigInteger bits2int(byte[] data, int qlen) {

        int dataLenBits = data.length * 8;
        BigInteger dataBignum = new BigInteger(1, data);
        BigInteger result;

        if (dataLenBits > qlen) {
            result = dataBignum.shiftRight(dataLenBits - qlen);
        } else {
            result = dataBignum;
        }

        return result;
    }

    private byte[] getBigIntegerBytes(BigInteger b) {

        byte[] buf = b.toByteArray();

        if (buf[0] == 0) {
            byte[] tmp = new byte[buf.length - 1];
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

        if (data.length * 8 > bitsLength) {
            return data;
        }

        int paddingLen;

        if (bitsLength % 8 > 0) {
            paddingLen = (bitsLength / 8 - data.length + 1);

        } else {
            paddingLen = (bitsLength / 8 - data.length);
        }

        byte[] result = new byte[paddingLen + data.length];
        System.arraycopy(data, 0, result, paddingLen, data.length);
        return result;
    }


    private BIG generateNonce(byte[] secretKey, byte[] data) throws Exception {

        byte[] dataHash = HashUtil.sha256(data);

        byte[] v = new byte[32];
        for (int i = 0; i < v.length; i++) v[i] = 1;

        byte[] k = new byte[32];

        for (int prefix = 0; prefix < 2; prefix++) {

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bos.write(v);
            bos.write(prefix);
            bos.write(secretKey);
            bos.write(dataHash);

            k = HashUtil.mac(bos.toByteArray(), k);
            v = HashUtil.mac(v, k);
        }

        v = HashUtil.mac(v, k);
        return BIG.fromBytesUnlimited(v);
    }

    private BIG hashPoints(ECP[] points) throws Exception {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        for (ECP point : points) {
            byte[] bufs = new byte[Constants.GROUP_G1_SIZE];
            point.toBytes(bufs, true);
            bos.write(bufs);
        }

        //System.out.println(Hex.toHexString(bos.toByteArray()));
        byte[] hashTrunc = new byte[32];
        System.arraycopy(bos.toByteArray(), 0, hashTrunc, 0, hashTrunc.length);
        byte[] hash = HashUtil.sha256omit(hashTrunc);
        return BIG.fromBytesUnlimited(hash);
    }

    public byte[] prove(byte[] secretKey, byte[] alpha) throws Exception {


        ECP publicKeyPoint = ECP.generator().mul(BIG.fromBytes(secretKey));
        ECP hPoint = hashToTryAndIncrement(publicKeyPoint, alpha);
        
        if (hPoint == null) return null;

        byte[] hString = ConvertUtil.parseECPByte(hPoint);
        BIG secret = BIG.fromBytes(secretKey);
        ECP gammaPoint = hPoint.mul(secret);
       
        BIG k = generateNonce(secretKey, hString);

        //ECP generator is a fixed G value generator
        ECP uPoint = ECP.generator().mul(k);
        ECP vPoint = hPoint.mul(k);
       
        BIG c = hashPoints(new ECP[]{hPoint, gammaPoint, uPoint, vPoint});
        BIG.fromBytes(secretKey);
        k.add(BIG.mul2(c, secret));
        k.mod(order);
        BIG s = k;


        byte[] gammaString = ConvertUtil.parseECPByte(gammaPoint);
        byte[] cString = ConvertUtil.parseBIGByte(c);
        byte[] sString = ConvertUtil.parseBIGByte(s);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(gammaString);
        int gammaInt = bos.size();
        bos.write(cString);
        int cSInt = bos.size();
        bos.write(sString);
        int sSInt = bos.size();

        bos.write(ByteBuffer.allocate(4).putInt(gammaInt).array());
        bos.write(ByteBuffer.allocate(4).putInt(cSInt).array());
        bos.write(ByteBuffer.allocate(4).putInt(sSInt).array());


        return bos.toByteArray();
    }

    private byte[] gammaToHash(ECP gamma) throws Exception {
        ECP gammaCof = gamma.mul(new BIG());
        byte[] gammaString = ConvertUtil.parseECPByte(gammaCof);

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
        try {
            int gammaInt = ByteBuffer.wrap(Arrays.copyOfRange(pi, pi.length - 12, pi.length - 8)).getInt();
            int cSInt = ByteBuffer.wrap(Arrays.copyOfRange(pi, pi.length - 8, pi.length - 4)).getInt();
            int sSInt = ByteBuffer.wrap(Arrays.copyOfRange(pi, pi.length - 4, pi.length)).getInt();
            
            byte[] gammaBytes = new byte[gammaInt];
            System.arraycopy(pi, 0, gammaBytes, 0, gammaInt);
            ECP gamma = ECP.fromBytes(gammaBytes);

            byte[] cBytes = new byte[cSInt - gammaInt];
            System.arraycopy(pi, gammaInt, cBytes, 0, cBytes.length);
            BIG c = BIG.fromBytes(cBytes);

            byte[] sBytes = new byte[sSInt - cSInt];
          
            System.arraycopy(pi, cSInt, sBytes, 0, sBytes.length);
            BIG s = BIG.fromBytes(sBytes);

            return new Object[]{gamma, c, s};
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("pi length is invalid");
        }

    }

    public byte[] verify(byte[] y, byte[] pi, byte[] alpha) throws Exception {

        if (pi == null) return null;
        ECP publicKeyPoint = ECP.fromBytes(y);
        Object[] objs = decodeProof(pi);
        ECP gammaPoint = (ECP) objs[0];
        BIG c = (BIG) objs[1];
        BIG s = (BIG) objs[2];

        ECP hPoint = hashToTryAndIncrement(publicKeyPoint, alpha);

        ECP sb = ECP.generator().mul(s);

        ECP cy = publicKeyPoint.mul(c);
        cy.neg();
        sb.add(cy);

        ECP sh = hPoint.mul(s);
        ECP cGamma = gammaPoint.mul(c);
        cGamma.neg();

        sh.add(cGamma);
      
        BIG derivedC = hashPoints(new ECP[]{hPoint, gammaPoint, sb, sh});

        if (!derivedC.equals(c)) {
            throw new IllegalArgumentException("VRF Computation failed");
        }
        return gammaToHash(gammaPoint);

    }

}

