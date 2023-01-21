package io.Adrestus.consensus;

import io.Adrestus.crypto.bls.model.BLSPublicKey;

public abstract class AbstractState {


    public abstract void onEnterState(BLSPublicKey blsPublicKey);

    public abstract boolean onActiveState();

}
