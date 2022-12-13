package io.Adrestus.core;

import com.google.common.base.Objects;
import io.Adrestus.crypto.elliptic.SignatureData;
import io.activej.serializer.annotations.Serialize;
import io.activej.serializer.annotations.SerializeClass;

import java.util.Comparator;


@SerializeClass(subclasses = {RegularTransaction.class, RewardsTransaction.class, StakingTransaction.class, DelegateTransaction.class})
public abstract class Transaction implements Cloneable, Comparable<Transaction>, Comparator<Transaction> {


    protected String Hash;
    protected TransactionType Type;
    protected StatusType Status;
    protected int ZoneFrom;
    protected int ZoneTo;
    protected String timestamp;
    protected int BlockNumber;
    protected String From;
    protected String To;
    protected double Amount;
    protected double AmountWithTransactionFee;
    protected int Nonce;
    protected SignatureData Signature;


    public Transaction() {
        this.Hash = "";
        this.Type = TransactionType.REGULAR;
        this.Status = StatusType.PENDING;
        this.ZoneFrom = 0;
        this.ZoneTo = 0;
        this.BlockNumber = 0;
        this.timestamp = "";
        this.From = "";
        this.To = "";
        this.Amount = 0;
        this.AmountWithTransactionFee = 0;
        this.Nonce = 0;
        this.Signature = new SignatureData();
    }

    public Transaction(TransactionType type) {
        this.Hash = "";
        this.Type = type;
        this.Status = StatusType.PENDING;
        this.ZoneFrom = 0;
        this.ZoneTo = 0;
        this.BlockNumber = 0;
        this.timestamp = "";
        this.From = "";
        this.To = "";
        this.Amount = 0;
        this.AmountWithTransactionFee = 0;
        this.Nonce = 0;
        this.Signature = new SignatureData();
    }

    public Transaction(String hash) {
        this.Hash = hash;
        this.Type = TransactionType.REGULAR;
        this.Status = StatusType.PENDING;
        this.ZoneFrom = 0;
        this.ZoneTo = 0;
        this.BlockNumber = 0;
        this.timestamp = "";
        this.From = "";
        this.To = "";
        this.Amount = 0;
        this.AmountWithTransactionFee = 0;
        this.Nonce = 0;
        this.Signature = new SignatureData();
    }

    public Transaction(String hash, TransactionType type, StatusType status, int zoneFrom, int zoneTo, String timestamp, int blockNumber, String from, String to, double amount, double AmountWithTransactionFee, int nonce, SignatureData signature) {
        this.Hash = hash;
        this.Type = type;
        this.Status = status;
        this.ZoneFrom = zoneFrom;
        this.ZoneTo = zoneTo;
        this.timestamp = timestamp;
        this.BlockNumber = blockNumber;
        this.From = from;
        this.To = to;
        this.Amount = amount;
        this.AmountWithTransactionFee = AmountWithTransactionFee;
        this.Nonce = nonce;
        this.Signature = signature;
    }

    @Serialize
    public TransactionType getType() {
        return Type;
    }

    public void setType(TransactionType type) {
        Type = type;
    }

    @Serialize
    public StatusType getStatus() {
        return Status;
    }

    public void setStatus(StatusType status) {
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
        this.Amount = amount;
    }

    @Serialize
    public double getAmountWithTransactionFee() {
        return AmountWithTransactionFee;
    }

    public void setAmountWithTransactionFee(double AmountWithTransactionFee) {
        this.AmountWithTransactionFee = AmountWithTransactionFee;
    }

    @Serialize
    public int getNonce() {
        return Nonce;
    }

    public void setNonce(int nonce) {
        this.Nonce = nonce;
    }

    @Serialize
    public SignatureData getSignature() {
        return Signature;
    }

    public void setSignature(SignatureData signature) {
        this.Signature = signature;
    }

    @Serialize
    public String getHash() {
        return Hash;
    }

    public void setHash(String hash) {
        this.Hash = hash;
    }

    @Override
    public int compareTo(Transaction transaction) {
        return this.Hash.compareTo(transaction.Hash);
    }

    @Override
    public int compare(Transaction u1, Transaction u2) {
        return u1.Hash.compareTo(u2.Hash);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return ZoneFrom == that.ZoneFrom && ZoneTo == that.ZoneTo && BlockNumber == that.BlockNumber && Double.compare(that.Amount, Amount) == 0 && Double.compare(that.AmountWithTransactionFee, AmountWithTransactionFee) == 0 && Nonce == that.Nonce && Objects.equal(Hash, that.Hash) && Type == that.Type && Status == that.Status && Objects.equal(timestamp, that.timestamp) && Objects.equal(From, that.From) && Objects.equal(To, that.To) && Objects.equal(Signature, that.Signature);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(Hash, Type, Status, ZoneFrom, ZoneTo, timestamp, BlockNumber, From, To, Amount, AmountWithTransactionFee, Nonce, Signature);
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
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
                ", AmountWithTransactionFee=" + AmountWithTransactionFee +
                ", Nonce=" + Nonce +
                ", Signature=" + Signature +
                '}';
    }
}
