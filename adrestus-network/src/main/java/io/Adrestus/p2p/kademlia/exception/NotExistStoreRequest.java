package io.Adrestus.p2p.kademlia.exception;

public class NotExistStoreRequest extends Exception {
    public NotExistStoreRequest() {
        super("Store request is not existed and is already under process");
    }
}
