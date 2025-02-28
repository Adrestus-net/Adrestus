package io.Adrestus.crypto.pca.cs.jna.gmp;

import com.sun.jna.Memory;

/**
 * Prototype taken from jna-gmp github project (https://github.com/square/jna-gmp)
 * <p>
 * You do not need to edit this class
 *
 * @author Adrian Pacurar
 */
public class MPZMemory extends Memory {
    public final mpz_t peer;

    MPZMemory() {
        super(mpz_t.SIZE);
        peer = new mpz_t(this);
        LibGMPLoader.getInstance().getGmp().__gmpz_init(peer);
    }

    @Override
    public void close() {
        peer.clear();
    }
}
