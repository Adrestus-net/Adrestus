package io.Adrestus.crypto.bls.utils;


import io.Adrestus.crypto.bls.constants.Constants;
import org.apache.milagro.amcl.BLS381.BIG;
import org.apache.milagro.amcl.BLS381.ECP;
import org.apache.milagro.amcl.BLS381.ROM;

public class ConvertUtil {
    public static final int WEIERSTRASS = 0;
    private static long[] wr = new long[7];
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

        for (int i = 0; i < b.length; i++) {
            m.fshl(8);
            wr[0] += (int) b[i + n] & 0xff;
        }
        return new BIG(wr);
    }

    public static ECP fromBytes(byte[] b) {
        byte[] t = new byte[b.length];
        BIG p = new BIG(b.length);

        for (int i = 0; i < b.length - 1; i++) {
            t[i] = b[i + 1];
        }
        BIG px = byte_to_BIG(t, 0);
        if (BIG.comp(px, p) >= 0) return new ECP();

        if (CURVETYPE == MONTGOMERY) {
            return new ECP(px);
        }
        if (b[0] == 0x02 || b[0] == 0x03) {
            return new ECP(px, (int) (b[0] & 1));
        }
        if (b[0] == 0x04) {
            for (int i = 0; i < p.nbits(); i++) {
                t[i] = b[i + BIG.MODBYTES + 1];
            }
            BIG py = byte_to_BIG(t, 0);
            return new ECP(px, py);
        }

        return new ECP();
    }

    public static byte[] ecp_tobytes(ECP ecp){
        byte[] buf = new byte[Constants.GROUP_G1_SIZE];
        ecp.toBytes(buf,true);
        return buf;
    }
}
