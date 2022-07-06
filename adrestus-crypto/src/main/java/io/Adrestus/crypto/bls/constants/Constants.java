package io.Adrestus.crypto.bls.constants;

import org.apache.milagro.amcl.BLS381.BIG;

public class Constants {

    final public static int MODBYTES = 48;
    final public static int BASEBITS = 58;
    final public static int CHUNK = 64;
    
    final public static int GROUP_G1_SIZE = (2 * MODBYTES + 1);
    
    final public static int FIELD_ELEMENT_SIZE = MODBYTES;
    
    final public static int CURVE_B_I = 4;
    
    final public static long[] CURVE_BNX = {0x201000000010000l, 0x34, 0x0, 0x0, 0x0, 0x0, 0x0};
    
    final public static int ATE_BITS = 65;
    
    final public static int NLEN = (1 + ((8 * MODBYTES - 1) / BASEBITS));
    final public static int DNLEN = 2*NLEN;

    final public static byte[] MESSAGE_DOMAIN_PREFIX = {0, 0};
    final public static byte[] VERKEY_DOMAIN_PREFIX = {1, 1};
    
    final public static long[] CURVE_ORDER = {
               0x3FFFFFF00000001l,
               0x36900BFFF96FFBFl,
               0x180809A1D80553Bl,
               0x14CA675F520CCE7l,
               0x73EDA7l,
               0x0l,
               0x0l
    };
    
    final public static BIG curveOrder = new BIG(Constants.CURVE_ORDER);
        
}
