package io.Adrestus.crypto;

import io.Adrestus.crypto.bls.model.*;
import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.jpbc.PairingParameters;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import org.junit.jupiter.api.Test;
import org.spongycastle.util.encoders.Hex;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BLS2Test {

    @Test
    public void testing(){
        Pairing pairing=PairingFactory.getPairing("C:\\Users\\User\\Documents\\GitHub\\Adrestus\\adrestus-crypto\\src\\test\\java\\io\\Adrestus\\crypto\\curve.properties");
        Element g = pairing.getG1().newRandomElement().getImmutable();
        Element priv = pairing.getZr().newRandomElement();
        Element pub = g.powZn(priv);
        System.out.println(Hex.toHexString(priv.toBytes()));
        System.out.println(Hex.toHexString(pub.toBytes()));
        System.out.println("00000000000000000000000000000000119f962794f815e4f21ae048e66286a930f69b51bbc711367a6a4b3a57e3f5ff".length());
        SigKey privs = new SigKey(new FieldElement("0000000000000000000000000000000019557ae7ec745e251ce48a05ad46828ee85650560baa78".getBytes(StandardCharsets.UTF_8)));
        VerKey pubs = new VerKey(new G1(pub.toBytes()));
       // System.out.println(Hex.toHexString(sk.toBytes()));
        //System.out.println(Hex.toHexString(vk.toBytes()));

        byte[] msg = "Test_Message".getBytes();
        Signature sig = new Signature(msg, privs);
       // assertEquals(true,sig.verify(msg, pubs, new G1(g.toBytes())));
    }
}
