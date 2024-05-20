package io.Adrestus.core;

import io.Adrestus.crypto.elliptic.ECDSASignatureData;

import java.math.BigInteger;

public class RegularTransaction extends Transaction {

    public RegularTransaction() {
        super(TransactionType.REGULAR);
    }

    public RegularTransaction(String hash, TransactionType type, StatusType status, int zoneFrom, int zoneTo, String timestamp, int blockNumber, String from, String to, double amount, double AmountWithTransactionFee, int nonce, ECDSASignatureData signature) {
        super(hash, type, status, zoneFrom, zoneTo, timestamp, blockNumber, from, to, amount, AmountWithTransactionFee, nonce, signature);
    }

    public RegularTransaction(String hash, TransactionType type, StatusType status, int zoneFrom, int zoneTo, String timestamp, String from, String to, double amount, double AmountWithTransactionFee, int nonce, ECDSASignatureData signature) {
        super(hash, type, status, zoneFrom, zoneTo, timestamp, from, to, amount, AmountWithTransactionFee, nonce, signature);
    }

    public RegularTransaction(String hash, TransactionType type, StatusType status, int zoneFrom, int zoneTo, String timestamp, int blockNumber, String from, String to, double amount, double AmountWithTransactionFee, int nonce, BigInteger XAxis, BigInteger YAxis, ECDSASignatureData signature) {
        super(hash, type, status, zoneFrom, zoneTo, timestamp, blockNumber, from, to, amount, AmountWithTransactionFee, nonce, XAxis, YAxis, signature);
    }

    public RegularTransaction(String hash) {
        super(hash);
    }

    @Override
    public void accept(TransactionUnitVisitor visitor) {
        visitor.visit(this);
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public void infos(String value) {
        if (this.transactionCallback == null)
            return;
        this.transactionCallback.call(value);
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public String getFrom() {
        return super.getFrom();
    }

    @Override
    public String getHash() {
        return super.getHash();
    }
}
