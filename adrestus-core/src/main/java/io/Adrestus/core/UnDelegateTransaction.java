package io.Adrestus.core;

import io.Adrestus.crypto.elliptic.ECDSASignatureData;

public class UnDelegateTransaction extends DelegateTransaction{

    public UnDelegateTransaction(String delegatorAddress, String validatorAddress) {
        super(delegatorAddress, validatorAddress);
    }

    public UnDelegateTransaction(String hash, TransactionType type, StatusType status, int zoneFrom, int zoneTo, String timestamp, int blockNumber, String from, String to, double amount, double transactionFee, int nonce, ECDSASignatureData signature, String delegatorAddress, String validatorAddress) {
        super(hash, type, status, zoneFrom, zoneTo, timestamp, blockNumber, from, to, amount, transactionFee, nonce, signature, delegatorAddress, validatorAddress);
    }

    public UnDelegateTransaction() {
        super();
    }

    @Override
    public void accept(TransactionUnitVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String getDelegatorAddress() {
        return super.getDelegatorAddress();
    }

    @Override
    public void setDelegatorAddress(String delegatorAddress) {
        super.setDelegatorAddress(delegatorAddress);
    }

    @Override
    public String getValidatorAddress() {
        return super.getValidatorAddress();
    }


    @Override
    public void infos(String value) {
        if(this.transactionCallback==null)
            return;
        this.transactionCallback.call(value);
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public void setValidatorAddress(String validatorAddress) {
        super.setValidatorAddress(validatorAddress);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
