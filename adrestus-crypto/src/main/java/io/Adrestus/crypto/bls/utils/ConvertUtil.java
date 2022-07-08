package io.Adrestus.crypto.bls.utils;


import io.Adrestus.crypto.bls.constants.Constants;
import org.apache.milagro.amcl.BLS381.BIG;
import org.apache.milagro.amcl.BLS381.ECP;
import org.apache.milagro.amcl.BLS381.ROM;

import java.lang.reflect.Field;

public class ConvertUtil {
    public static final int WEIERSTRASS = 0;
    public static final int MODBYTES = 48; //(1+(MODBITS-1)/8);
    public static final int BASEBITS = 58;

    public static final int NLEN = (1 + ((8 * MODBYTES - 1) / BASEBITS));
    public static final int MONTGOMERY = 2;
    public static final int CURVETYPE = WEIERSTRASS;

    public static ECP mapit(byte[] h) {
        BIG q = new BIG(ROM.Modulus);
        BIG x = byte_to_BIG(h, 0);
        x.mod(q);

        ECP P;
        do {
            do {
                P = new ECP(x, 0);
                x.inc(1);
                x.norm();
            } while (P.is_infinity());

            P.cfp();
        } while (P.is_infinity());

        return P;
    }

    public static BIG byte_to_BIG(byte[] b, int n) {
        BIG m = new BIG(0);
        Class myClass = m.getClass();
        Field myField = null;
        long[] wr = new long[NLEN];

        try {
            myField = ConvertUtil.getField(myClass, "w");
            myField.setAccessible(true);
            wr[0]=0;
            for (int i=1;i<NLEN;i++)
                wr[i]=0;
            myField.set(m,wr);
            for (int i = 0; i < b.length; i++) {
                m.fshl(8);
                 wr[0] += (int) b[i + n] & 0xff;
                 myField.set(m,wr);
            }
            return m;
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return new BIG();
    }

    public static ECP fromBytes(byte[] b) {
        byte[] t=new byte[BIG.MODBYTES];
        BIG p=new BIG(ROM.Modulus);

        for (int i=0;i<BIG.MODBYTES;i++) t[i]=b[i+1];
        BIG px=BIG.fromBytes(t);
        if (BIG.comp(px,p)>=0) return new ECP();

        if (CURVETYPE==MONTGOMERY)
        {
            return new ECP(px);
        }

        if (b[0]==0x04)
        {
            for (int i=0;i<BIG.MODBYTES;i++) t[i]=b[i+BIG.MODBYTES+1];
            BIG py=BIG.fromBytes(t);
            if (BIG.comp(py,p)>=0) return new ECP();
            return new ECP(px,py);
        }

        if (b[0]==0x02 || b[0]==0x03)
        {
            return new ECP(px,(int)(b[0]&1));
        }
        return new ECP();
    }

    public static byte[] ecp_tobytes(ECP ecp) {
        byte[] buf = new byte[Constants.GROUP_G1_SIZE];
        ecp.toBytes(buf, true);
        return buf;
    }

    private static Field getField(Class clazz, String fieldName)
            throws NoSuchFieldException {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            Class superClass = clazz.getSuperclass();
            if (superClass == null) {
                throw e;
            } else {
                return getField(superClass, fieldName);
            }
        }
    }

}
