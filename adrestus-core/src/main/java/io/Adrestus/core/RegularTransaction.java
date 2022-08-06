package io.Adrestus.core;

import io.Adrestus.crypto.elliptic.SignatureData;

public class RegularTransaction extends Transaction {

    public RegularTransaction() {
    }

    public RegularTransaction(String hash, TransactionType type, TransactionStatus status, int zoneFrom, int zoneTo, String timestamp, int blockNumber, String from, String to, double amount, double AmountWithTransactionFee, int nonce, SignatureData signature) {
        super(hash, type, status, zoneFrom, zoneTo, timestamp, blockNumber, from, to, amount, AmountWithTransactionFee, nonce, signature);
    }


    @Override
    public String toString() {
        return "RegularTransaction{" +
                "Hash='" + Hash + '\'' +
                ", Type=" + Type +
                ", Status=" + Status +
                ", ZoneFrom=" + ZoneFrom +
                ", ZoneTo=" + ZoneTo +
                ", timestamp='" + timestamp + '\'' +
                ", BlockNumber=" + BlockNumber +
                ", From='" + From + '\'' +
                ", To='" + To + '\'' +
                ", Amount=" + Amount +
                ", AmountWithTransactionFee=" + AmountWithTransactionFee +
                ", Nonce=" + Nonce +
                ", Signature=" + Signature +
                '}';
    }
}
