package io.Adrestus.core;

import io.Adrestus.crypto.elliptic.SignatureData;
import io.Adrestus.util.GetTime;

import java.sql.Timestamp;
import java.util.Objects;

public class Transaction {


    private TransactionType Type;
    private TransactionStatus Status;
    private int ZoneFrom;
    private int ZoneTo;
    private String Hash;
    private int BlockNumber;
    private Timestamp Timestamp;
    private String From;
    private String to;
    private double Amount;
    private double TransactionFee;
    private int Nonce;
    private SignatureData Signature;


    public Transaction(TransactionStatus status, int zoneFrom, int zoneTo, int blockNumber, java.sql.Timestamp timestamp, String from, String to, double amount, double transactionFee, int nonce, SignatureData signature) {
        this.Status = status;
        this.ZoneFrom = zoneFrom;
        this.ZoneTo = zoneTo;
        this.BlockNumber = blockNumber;
        this.Timestamp = timestamp;
        this.From = from;
        this.to = to;
        this.Amount = amount;
        this.TransactionFee = transactionFee;
        this.Nonce = nonce;
        this.Signature = signature;
    }

    public Transaction(TransactionStatus status, int zoneFrom, int zoneTo, int blockNumber, String from, String to, double amount, SignatureData signature) {
        this.Status = status;
        this.ZoneFrom = zoneFrom;
        this.ZoneTo = zoneTo;
        this.BlockNumber = blockNumber;
        this.From = from;
        this.to = to;
        this.Amount = amount;
        this.Signature = signature;
    }

    public Transaction(TransactionType Type,TransactionStatus status, int zoneFrom, int blockNumber, String from,double Amount,int Nonce, SignatureData signature) {
        this.Type=Type;
        this.Status = status;
        this.ZoneFrom = zoneFrom;
        this.BlockNumber = blockNumber;
        this.Timestamp= GetTime.GetTimeStamp();
        this.From = from;
        this.Amount=Amount;
        this.Nonce=Nonce;
        this.Signature = signature;
    }

    public TransactionStatus getStatus() {
        return Status;
    }

    public void setStatus(TransactionStatus status) {
        Status = status;
    }

    public int getZoneFrom() {
        return ZoneFrom;
    }

    public void setZoneFrom(int zoneFrom) {
        ZoneFrom = zoneFrom;
    }

    public int getZoneTo() {
        return ZoneTo;
    }

    public void setZoneTo(int zoneTo) {
        ZoneTo = zoneTo;
    }

    public String getHash() {
        return Hash;
    }

    public void setHash(String hash) {
        Hash = hash;
    }

    public int getBlockNumber() {
        return BlockNumber;
    }

    public void setBlockNumber(int blockNumber) {
        BlockNumber = blockNumber;
    }

    public java.sql.Timestamp getTimestamp() {
        return Timestamp;
    }

    public void setTimestamp(java.sql.Timestamp timestamp) {
        Timestamp = timestamp;
    }

    public String getFrom() {
        return From;
    }

    public void setFrom(String from) {
        From = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public double getAmount() {
        return Amount;
    }

    public void setAmount(double amount) {
        Amount = amount;
    }

    public double getTransactionFee() {
        return TransactionFee;
    }

    public void setTransactionFee(double transactionFee) {
        TransactionFee = transactionFee;
    }

    public int getNonce() {
        return Nonce;
    }

    public void setNonce(int nonce) {
        Nonce = nonce;
    }

    public SignatureData getSignature() {
        return Signature;
    }

    public void setSignature(SignatureData signature) {
        Signature = signature;
    }


    public TransactionType getType() {
        return Type;
    }

    public void setType(TransactionType type) {
        Type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return ZoneFrom == that.ZoneFrom && ZoneTo == that.ZoneTo && BlockNumber == that.BlockNumber && Double.compare(that.Amount, Amount) == 0 && Double.compare(that.TransactionFee, TransactionFee) == 0 && Nonce == that.Nonce && Type == that.Type && Status == that.Status && Objects.equals(Hash, that.Hash) && Objects.equals(Timestamp, that.Timestamp) && Objects.equals(From, that.From) && Objects.equals(to, that.to) && Objects.equals(Signature, that.Signature);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Type, Status, ZoneFrom, ZoneTo, Hash, BlockNumber, Timestamp, From, to, Amount, TransactionFee, Nonce, Signature);
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "Type=" + Type +
                ", Status=" + Status +
                ", ZoneFrom=" + ZoneFrom +
                ", ZoneTo=" + ZoneTo +
                ", Hash='" + Hash + '\'' +
                ", BlockNumber=" + BlockNumber +
                ", Timestamp=" + Timestamp +
                ", From='" + From + '\'' +
                ", to='" + to + '\'' +
                ", Amount=" + Amount +
                ", TransactionFee=" + TransactionFee +
                ", Nonce=" + Nonce +
                ", Signature=" + Signature +
                '}';
    }
}
