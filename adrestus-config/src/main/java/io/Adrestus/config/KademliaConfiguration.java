package io.Adrestus.config;


import java.math.BigInteger;

public class KademliaConfiguration {
    public static final int PORT = 8081;
    // public static final BLSPrivateKey sk1 = new BLSPrivateKey(1);
    public static final String BLSPublicKeyHex = "04082909431bc6c2adc38d791d132828e1ee3a034bc60b2d37944005b1175b1e9b4c7783eeb1b53b330bce60e18ae85a33145db8cae9d7df6297bec8b0ac5f355f87bed6e55b40962a9ecfa69464aeb36c8265ebb3385214cb5a5961598cb7b365";
    public static final String BootstrapNodeIP = "localhost";
    public static final int BootstrapNodePORT = 8080;
    public static final BigInteger BootstrapNodeID = BigInteger.valueOf(1);
}
