package io.Adrestus.consensus;

import io.Adrestus.crypto.bls.model.BLSPublicKey;

public abstract class AbstractState implements Cloneable{


    public abstract void onEnterState(BLSPublicKey blsPublicKey);

    public abstract boolean onActiveState();

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
