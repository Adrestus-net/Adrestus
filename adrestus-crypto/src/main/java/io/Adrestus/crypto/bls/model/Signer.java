package io.Adrestus.crypto.bls.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Signer {

    public int id;
    private BLSPublicKey publicKey;
    private BLSPrivateKey privateKey;

    public Signer(int id, BLSPublicKey publicKey, BLSPrivateKey privateKey) {
        this.id = id;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    public static List<Signer> keygenFromShares(int numSigners, Map<Integer, FieldElement> xShares, Params params) {
        List<Signer> signers = new ArrayList<>();
        for (int i = 0; i < numSigners; i++) {
            int id = i + 1;
            FieldElement xi = xShares.remove(id);
            G1Point gxi = new G1Point(params.g.getValue().mul(xi.value));
            signers.add(new Signer(id, new BLSPublicKey(gxi), new BLSPrivateKey(xi)));
        }
        return signers;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public BLSPublicKey getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(BLSPublicKey publicKey) {
        this.publicKey = publicKey;
    }

    public BLSPrivateKey getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(BLSPrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Signer signer = (Signer) o;
        return id == signer.id && Objects.equals(publicKey, signer.publicKey) && Objects.equals(privateKey, signer.privateKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, publicKey, privateKey);
    }
}
