package io.Adrestus.crypto.bls.utils;



import io.Adrestus.crypto.bls.BLS381.BIG;
import io.Adrestus.crypto.bls.BLS381.ECP;
import io.Adrestus.crypto.bls.constants.Constants;

public class ConvertUtil {

    public static byte[] parseECPByte(ECP ecp) {
        byte[] buf = new byte[Constants.GROUP_G1_SIZE];
        ecp.toBytes(buf, true);
        return buf;
    }

    public static byte[] parseBIGByte(BIG ecp) {
        byte[] buf = new byte[Constants.GROUP_G1_SIZE];
        ecp.toBytes(buf);
        return buf;
    }


}
