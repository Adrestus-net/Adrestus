package io.Adrestus.core;

import com.google.common.base.Objects;
import io.Adrestus.crypto.elliptic.ECDSASignature;
import io.activej.serializer.annotations.Serialize;

public class Transaction {


    protected  String Hash;
    protected  TransactionType Type;
    protected  TransactionStatus Status;
    protected  int ZoneFrom;
    protected  int ZoneTo;
    protected  String timestamp;
    protected  int BlockNumber;
    protected  String From;
    protected  String To;
    protected  double Amount;
    protected  double TransactionFee;
    protected  int Nonce;
    protected  ECDSASignature Signature;


    public Transaction() {
        this.Hash = "";
        this.Type = TransactionType.ORDINARY;
        this.Status = TransactionStatus.PENDING;
        this.ZoneFrom = 0;
        this.ZoneTo = 0;
        this.BlockNumber = 0;
        this.timestamp = "";
        this.From = "";
        this.To = "";
        this.Amount = 0;
        this.TransactionFee = 0;
        this.Nonce = 0;
        this.Signature = new ECDSASignature();
    }

    public Transaction(String hash, TransactionType type, TransactionStatus status, int zoneFrom, int zoneTo, String timestamp, int blockNumber, String from, String to, double amount, double transactionFee, int nonce, ECDSASignature signature) {
        Hash = hash;
        Type = type;
        Status = status;
        ZoneFrom = zoneFrom;
        ZoneTo = zoneTo;
        this.timestamp = timestamp;
        BlockNumber = blockNumber;
        From = from;
        To = to;
        Amount = amount;
        TransactionFee = transactionFee;
        Nonce = nonce;
        Signature = signature;
    }

    @Serialize
    public TransactionType getType() {
        return Type;
    }

    public void setType(TransactionType type) {
        Type = type;
    }

    @Serialize
    public TransactionStatus getStatus() {
        return Status;
    }

    public void setStatus(TransactionStatus status) {
        Status = status;
    }

    @Serialize
    public int getZoneFrom() {
        return ZoneFrom;
    }

    public void setZoneFrom(int zoneFrom) {
        ZoneFrom = zoneFrom;
    }

    @Serialize
    public int getZoneTo() {
        return ZoneTo;
    }

    public void setZoneTo(int zoneTo) {
        ZoneTo = zoneTo;
    }

    @Serialize
    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Serialize
    public int getBlockNumber() {
        return BlockNumber;
    }

    public void setBlockNumber(int blockNumber) {
        BlockNumber = blockNumber;
    }

    @Serialize
    public String getFrom() {
        return From;
    }

    public void setFrom(String from) {
        From = from;
    }

    @Serialize
    public String getTo() {
        return To;
    }

    public void setTo(String to) {
        To = to;
    }

    @Serialize
    public double getAmount() {
        return Amount;
    }

    public void setAmount(double amount) {
        Amount = amount;
    }

    @Serialize
    public double getTransactionFee() {
        return TransactionFee;
    }

    public void setTransactionFee(double transactionFee) {
        TransactionFee = transactionFee;
    }

    @Serialize
    public int getNonce() {
        return Nonce;
    }

    public void setNonce(int nonce) {
        Nonce = nonce;
    }

    @Serialize
    public ECDSASignature getSignature() {
        return Signature;
    }

    public void setSignature(ECDSASignature signature) {
        Signature = signature;
    }

    @Serialize
    public String getHash() {
        return Hash;
    }

    public void setHash(String hash) {
        Hash = hash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return ZoneFrom == that.ZoneFrom && ZoneTo == that.ZoneTo && BlockNumber == that.BlockNumber && Double.compare(that.Amount, Amount) == 0 && Double.compare(that.TransactionFee, TransactionFee) == 0 && Nonce == that.Nonce && Objects.equal(Hash, that.Hash) && Type == that.Type && Status == that.Status && Objects.equal(timestamp, that.timestamp) && Objects.equal(From, that.From) && Objects.equal(To, that.To) && Objects.equal(Signature, that.Signature);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(Hash, Type, Status, ZoneFrom, ZoneTo, timestamp, BlockNumber, From, To, Amount, TransactionFee, Nonce, Signature);
    }

    public void d(){
        System.out.println("transaction");
    }
    @Override
    public String toString() {
        return "Transaction{" +
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
                ", TransactionFee=" + TransactionFee +
                ", Nonce=" + Nonce +
                ", Signature=" + Signature +
                '}';
    }
}
