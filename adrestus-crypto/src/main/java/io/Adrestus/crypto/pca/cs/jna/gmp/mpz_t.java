package io.Adrestus.crypto.pca.cs.jna.gmp;

import com.sun.jna.Pointer;

/**
 * The data type to pass to libgmp functions (gmp integers of type mpz_t)
 * <p>
 * Prototype taken from jna-gmp github project (https://github.com/square/jna-gmp)
 * <p>
 * You do not need to edit this class.
 *
 * @author Adi Pacurar
 */
public class mpz_t extends Pointer {
    public static final int SIZE = 16;//size in bytes of the native structures
    private final long peer;

    /**
     * Construct a long from a native address.
     *
     * @param peer the address of a block of native memory at least SIZE bytes large
     */
    public mpz_t(long peer) {
        super(peer);
        this.peer = peer;
    }

    /**
     * Constructs mpz_t from a Pointer
     *
     * @param from
     */
    public mpz_t(Pointer from) {
        this(Pointer.nativeValue(from));
    }

    public void clear() {
        if (super.peer != 0) {
            LibGMPLoader.getInstance().getGmp().__gmpz_clear(this);
        }
    }
}
