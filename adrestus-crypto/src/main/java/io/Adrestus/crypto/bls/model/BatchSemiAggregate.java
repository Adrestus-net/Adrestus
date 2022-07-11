package io.Adrestus.crypto.bls.model;

public class BatchSemiAggregate {
    private final G2Point sigPoint;
    private final GTPoint msgPubKeyPairing;

    public BatchSemiAggregate(G2Point sigPoint, GTPoint msgPubKeyPairing) {
        this.sigPoint = sigPoint;
        this.msgPubKeyPairing = msgPubKeyPairing;
    }

   public G2Point getSigPoint() {
        return sigPoint;
    }

    public GTPoint getMsgPubKeyPairing() {
        return msgPubKeyPairing;
    }
}
