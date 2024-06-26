package io.Adrestus.config;

public class AdrestusConfiguration {
    public static final int version = 0x00;
    public static final int BUFFER_SIZE = 64 * 1024;
    public static final int PIERRZAK_BIT = 2048;
    public static final int CORES = Runtime.getRuntime().availableProcessors();

    public static final int MAXIMU_BLOCK_SIZE = 10000000;

    public static final int INIT_VDF_DIFFICULTY = 100;

    public static final int MAX_ZONES = 3;

    public static final String ALGORITHM = "SHA1PRNG";

    public static final String PROVIDER = "SUN";


    public static final String SIGN_ALGORITHM = "EC";
    public static final String SIGN_PROVIDER = "BC";

    public static final String SIGN_CURVE = "secp256k1";


    public static final int TRANSACTIONS_QUEUE_SIZE = 2048 * 100;
    public static final int BLOCK_QUEUE_SIZE = 1024;
}
