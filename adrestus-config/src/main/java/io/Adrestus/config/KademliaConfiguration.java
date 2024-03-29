package io.Adrestus.config;


import java.math.BigInteger;
import java.util.concurrent.TimeUnit;

import static io.Adrestus.config.ConsensusConfiguration.EPOCH_TRANSITION;

public class KademliaConfiguration {
    public static final int PORT = 8081;
    // public static final BLSPrivateKey sk1 = new BLSPrivateKey(1);
    public static final String BLSPublicKeyHex = "04082909431bc6c2adc38d791d132828e1ee3a034bc60b2d37944005b1175b1e9b4c7783eeb1b53b330bce60e18ae85a33145db8cae9d7df6297bec8b0ac5f355f87bed6e55b40962a9ecfa69464aeb36c8265ebb3385214cb5a5961598cb7b365";
    public static final String LOCAL_NODE_IP = "127.0.0.1";
    public static final String BOOTSTRAP_NODE_IP = "192.168.1.106";
    public static final int BootstrapNodePORT = 8080;
    public static final BigInteger BootstrapNodeID = new BigInteger("000");
    public static int STORE_DELAY = 4 * 1000;

    public static int KADEMLIA_ROUTING_TABLE_DELAY = (EPOCH_TRANSITION - 5) * 2000;
    //public static int KADEMLIA_ROUTING_TABLE_DELAY = 1 * 2000;
    public static final int KADEMLIA_GET_TIMEOUT = 5;

    public static int IDENTIFIER_SIZE = 4;
    public static int BUCKET_SIZE = 10;
    public static int MAXIMUM_STORE_AND_LOOKUP_TIMEOUT_VALUE = 1;
    public static TimeUnit MAXIMUM_STORE_AND_LOOKUP_TIMEOUT_TIME_UNIT = TimeUnit.SECONDS;
    public static boolean ENABLED_FIRST_STORE_REQUEST_FORCE_PASS = true;
    public static TimeUnit PING_SCHEDULE_TIME_UNIT = TimeUnit.SECONDS;
    public static int PING_SCHEDULE_TIME_VALUE = 2;
    public static int ALPHA = 10;
    public static int FIND_NODE_SIZE = 20;
    public static int MAXIMUM_LAST_SEEN_AGE_TO_CONSIDER_ALIVE = 20;
    public static int DHT_EXECUTOR_POOL_SIZE = 20;
    public static int DHT_SCHEDULED_EXECUTOR_POOL_SIZE = 5;
    public static int SCHEDULED_EXECUTOR_POOL_SIZE = 1;
}
