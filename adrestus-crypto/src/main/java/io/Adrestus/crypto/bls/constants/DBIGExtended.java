package io.Adrestus.crypto.bls.constants;

import static com.google.common.base.Preconditions.checkArgument;

import org.apache.milagro.amcl.BLS381.BIG;
import org.apache.milagro.amcl.BLS381.DBIG;

public class DBIGExtended extends DBIG {


    DBIGExtended(DBIG dbig) {
        super(new DBIG(dbig));
    }


    DBIGExtended(byte[] bytes) {
        super(0);
        checkArgument(bytes.length % 4 == 0, "The number of bytes must be a multiple of 4.");
        checkArgument(bytes.length <= 2 * BIG.MODBYTES, "Too many bytes to store in a DBIG.");
        // Unroll the loop as an optimization that reduces the number of shl() operations
        w[0] |=
                ((((long) bytes[0] & 0xff) << 24)
                        | (((long) bytes[1] & 0xff) << 16)
                        | (((long) bytes[2] & 0xff) << 8)
                        | ((long) bytes[3] & 0xff));
        for (int i = 4; i < bytes.length; i += 4) {
            shl(32);
            w[0] |=
                    ((((long) bytes[i] & 0xff) << 24)
                            | (((long) bytes[i + 1] & 0xff) << 16)
                            | (((long) bytes[i + 2] & 0xff) << 8)
                            | ((long) bytes[i + 3] & 0xff));
        }
    }

    boolean isOdd() {
        return (w[0] & 1) == 1;
    }

    DBIGExtended fshr(int k) {
        this.shr(k);
        return this;
    }
}
