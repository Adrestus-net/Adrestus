package io.Adrestus.core;

import com.google.common.base.Objects;
import io.Adrestus.crypto.elliptic.ECDSASignatureData;
import io.activej.serializer.annotations.Serialize;
import io.activej.serializer.annotations.SerializeClass;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Comparator;


@SerializeClass(subclasses = {RegularTransaction.class, RewardsTransaction.class, StakingTransaction.class, DelegateTransaction.class})
public abstract class Transaction implements Cloneable, Comparable<Transaction>, Comparator<Transaction>, Serializable {


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

    protected BigInteger XAxis;
    protected BigInteger YAxis;
    protected ECDSASignatureData Signature;


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
        this.XAxis=new BigInteger("0");
        this.YAxis=new BigInteger("0");
        this.Signature = new ECDSASignatureData();
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
        this.XAxis=new BigInteger("0");
        this.YAxis=new BigInteger("0");
        this.Signature = new ECDSASignatureData();
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
        this.XAxis=new BigInteger("0");
        this.YAxis=new BigInteger("0");
        this.Signature = new ECDSASignatureData();
    }

    public Transaction(String hash, TransactionType type, StatusType status, int zoneFrom, int zoneTo, String timestamp, int blockNumber, String from, String to, double amount, double AmountWithTransactionFee, int nonce, ECDSASignatureData signature) {
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

    public Transaction(String hash, TransactionType type, StatusType status, int zoneFrom, int zoneTo, String timestamp, int blockNumber, String from, String to, double amount, double amountWithTransactionFee, int nonce, String signerPub, BigInteger XAxis, BigInteger YAxis, ECDSASignatureData signature) {
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
        this.AmountWithTransactionFee = amountWithTransactionFee;
        this.Nonce = nonce;
        this.XAxis = XAxis;
        this.YAxis = YAxis;
        this.Signature = signature;
    }

    @Serialize
    public TransactionType getType() {
        return  this.Type;
    }

    public void setType(TransactionType type) {
        this.Type = type;
    }

    @Serialize
    public StatusType getStatus() {
        return  this.Status;
    }

    public void setStatus(StatusType status) {
        this.Status = status;
    }

    @Serialize
    public int getZoneFrom() {
        return  this.ZoneFrom;
    }

    public void setZoneFrom(int zoneFrom) {
        this.ZoneFrom = zoneFrom;
    }

    @Serialize
    public int getZoneTo() {
        return  this.ZoneTo;
    }

    public void setZoneTo(int zoneTo) {
        this.ZoneTo = zoneTo;
    }

    @Serialize
    public String getTimestamp() {
        return  this.timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Serialize
    public int getBlockNumber() {
        return  this.BlockNumber;
    }

    public void setBlockNumber(int blockNumber) {
        this.BlockNumber = blockNumber;
    }

    @Serialize
    public String getFrom() {
        return  this.From;
    }

    public void setFrom(String from) {
        this.From = from;
    }

    @Serialize
    public String getTo() {
        return  this.To;
    }

    public void setTo(String to) {
        this.To = to;
    }

    @Serialize
    public double getAmount() {
        return  this.Amount;
    }

    public void setAmount(double amount) {
        this.Amount = amount;
    }

    @Serialize
    public double getAmountWithTransactionFee() {
        return  this.AmountWithTransactionFee;
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
    public ECDSASignatureData getSignature() {
        return  this.Signature;
    }

    public void setSignature(ECDSASignatureData signature) {
        this.Signature = signature;
    }

    @Serialize
    public String getHash() {
        return  this.Hash;
    }

    public void setHash(String hash) {
        this.Hash = hash;
    }

    @Serialize
    public BigInteger getXAxis() {
        return  this.XAxis;
    }

    public void setXAxis(BigInteger XAxis) {
        this.XAxis = XAxis;
    }

    @Serialize
    public BigInteger getYAxis() {
        return YAxis;
    }

    public void setYAxis(BigInteger YAxis) {
        this.YAxis = YAxis;
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
        return ZoneFrom == that.ZoneFrom && ZoneTo == that.ZoneTo && BlockNumber == that.BlockNumber && Double.compare(that.Amount, Amount) == 0 && Double.compare(that.AmountWithTransactionFee, AmountWithTransactionFee) == 0 && Nonce == that.Nonce && Objects.equal(Hash, that.Hash) && Type == that.Type && Status == that.Status && Objects.equal(timestamp, that.timestamp) && Objects.equal(From, that.From) && Objects.equal(To, that.To)  && Objects.equal(XAxis, that.XAxis) && Objects.equal(YAxis, that.YAxis) && Objects.equal(Signature, that.Signature);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(Hash, Type, Status, ZoneFrom, ZoneTo, timestamp, BlockNumber, From, To, Amount, AmountWithTransactionFee, Nonce, XAxis, YAxis, Signature);
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
                ", XAxis=" + XAxis +
                ", YAxis=" + YAxis +
                ", Signature=" + Signature +
                '}';
    }
}
