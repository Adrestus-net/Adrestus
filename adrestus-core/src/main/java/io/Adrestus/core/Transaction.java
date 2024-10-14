package io.Adrestus.core;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.Adrestus.core.mapper.SerializerCoreActiveJ;
import io.Adrestus.crypto.elliptic.ECDSASignatureData;
import io.activej.serializer.annotations.Serialize;
import io.activej.serializer.annotations.SerializeClass;
import io.activej.serializer.annotations.SerializeNullable;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Comparator;
import java.util.Objects;


@SerializeClass(subclasses = {RegularTransaction.class, RewardsTransaction.class, StakingTransaction.class, DelegateTransaction.class, UnclaimedFeeRewardTransaction.class, UnDelegateTransaction.class, UnstakingTransaction.class})
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "transactiontype")
@JsonSubTypes({@JsonSubTypes.Type(value = RegularTransaction.class, name = "RegularTransaction"), @JsonSubTypes.Type(value = RewardsTransaction.class, name = "RewardsTransaction"), @JsonSubTypes.Type(value = StakingTransaction.class, name = "StakingTransaction"), @JsonSubTypes.Type(value = DelegateTransaction.class, name = "DelegateTransaction"), @JsonSubTypes.Type(value = UnclaimedFeeRewardTransaction.class, name = "UnclaimedFeeRewardTransaction"), @JsonSubTypes.Type(value = UnDelegateTransaction.class, name = "UnDelegateTransaction"), @JsonSubTypes.Type(value = UnstakingTransaction.class, name = "UnstakingTransaction")})
@JsonPropertyOrder({"transactiontype", "type", "status", "timestamp", "hash", "nonce", "blockNumber", "from", "to", "zoneFrom", "zoneTo", "blockNumber", "amount", "amountWithTransactionFee", "xaxis", "yaxis", "signature", "transactionCallback"})
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
    protected BigDecimal Amount;
    protected BigDecimal AmountWithTransactionFee;
    protected int Nonce;

    protected BigInteger XAxis;
    protected BigInteger YAxis;
    protected ECDSASignatureData Signature;

    protected TransactionCallback transactionCallback;


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
        this.Amount = BigDecimal.ZERO;
        this.AmountWithTransactionFee = BigDecimal.ZERO;
        this.Nonce = 0;
        this.XAxis = new BigInteger("0");
        this.YAxis = new BigInteger("0");
        this.Signature = new ECDSASignatureData();
        this.transactionCallback = null;
    }

    public Transaction(TransactionType transactionType, BigDecimal amount) {
        this.Hash = "";
        this.Type = transactionType;
        this.Status = StatusType.PENDING;
        this.ZoneFrom = 0;
        this.ZoneTo = 0;
        this.BlockNumber = 0;
        this.timestamp = "";
        this.From = "";
        this.To = "";
        this.Amount = amount;
        this.AmountWithTransactionFee = BigDecimal.ZERO;
        this.Nonce = 0;
        this.XAxis = new BigInteger("0");
        this.YAxis = new BigInteger("0");
        this.Signature = new ECDSASignatureData();
        this.transactionCallback = null;
    }

    public Transaction(TransactionType type, Transaction... children) {
        this.Hash = "";
        this.Type = type;
        this.Status = StatusType.PENDING;
        this.ZoneFrom = 0;
        this.ZoneTo = 0;
        this.BlockNumber = 0;
        this.timestamp = "";
        this.From = "";
        this.To = "";
        this.Amount = BigDecimal.ZERO;
        this.AmountWithTransactionFee = BigDecimal.ZERO;
        this.Nonce = 0;
        this.XAxis = new BigInteger("0");
        this.YAxis = new BigInteger("0");
        this.Signature = new ECDSASignatureData();
        this.transactionCallback = null;
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
        this.Amount = BigDecimal.ZERO;
        this.AmountWithTransactionFee = BigDecimal.ZERO;
        this.Nonce = 0;
        this.XAxis = new BigInteger("0");
        this.YAxis = new BigInteger("0");
        this.Signature = new ECDSASignatureData();
        this.transactionCallback = null;
    }

    public Transaction(String hash, TransactionType type, StatusType status, int zoneFrom, int zoneTo, String timestamp, int blockNumber, String from, String to, BigDecimal amount, BigDecimal AmountWithTransactionFee, int nonce, ECDSASignatureData signature) {
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
        this.transactionCallback = null;
    }

    public Transaction(String hash, TransactionType type, StatusType status, int zoneFrom, int zoneTo, String timestamp, String from, String to, BigDecimal amount, BigDecimal AmountWithTransactionFee, int nonce, ECDSASignatureData signature) {
        this.Hash = hash;
        this.Type = type;
        this.Status = status;
        this.ZoneFrom = zoneFrom;
        this.ZoneTo = zoneTo;
        this.timestamp = timestamp;
        this.From = from;
        this.To = to;
        this.Amount = amount;
        this.AmountWithTransactionFee = AmountWithTransactionFee;
        this.Nonce = nonce;
        this.Signature = signature;
        this.transactionCallback = null;
    }

    public Transaction(String hash, TransactionType type, StatusType status, int zoneFrom, int zoneTo, String timestamp, int blockNumber, String from, String to, BigDecimal amount, BigDecimal amountWithTransactionFee, int nonce, BigInteger XAxis, BigInteger YAxis, ECDSASignatureData signature) {
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
        this.transactionCallback = null;
    }

    public void accept(TransactionUnitVisitor visitor) {
        this.accept(visitor);
    }

    @Serialize
    public TransactionType getType() {
        return this.Type;
    }

    public void setType(TransactionType type) {
        this.Type = type;
    }

    @Serialize
    public StatusType getStatus() {
        return this.Status;
    }

    public void setStatus(StatusType status) {
        this.Status = status;
    }

    @Serialize
    public int getZoneFrom() {
        return this.ZoneFrom;
    }

    public void setZoneFrom(int zoneFrom) {
        this.ZoneFrom = zoneFrom;
    }

    @Serialize
    public int getZoneTo() {
        return this.ZoneTo;
    }

    public void setZoneTo(int zoneTo) {
        this.ZoneTo = zoneTo;
    }

    @Serialize
    public String getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Serialize
    public int getBlockNumber() {
        return this.BlockNumber;
    }

    public void setBlockNumber(int blockNumber) {
        this.BlockNumber = blockNumber;
    }

    @Serialize
    public String getFrom() {
        return this.From;
    }

    public void setFrom(String from) {
        this.From = from;
    }

    @Serialize
    public String getTo() {
        return this.To;
    }

    public void setTo(String to) {
        this.To = to;
    }

    @Serialize
    public BigDecimal getAmount() {
        return Amount;
    }

    public void setAmount(BigDecimal amount) {
        Amount = amount;
    }

    @Serialize
    public BigDecimal getAmountWithTransactionFee() {
        return AmountWithTransactionFee;
    }

    public void setAmountWithTransactionFee(BigDecimal amountWithTransactionFee) {
        AmountWithTransactionFee = amountWithTransactionFee;
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
        return this.Signature;
    }

    public void setSignature(ECDSASignatureData signature) {
        this.Signature = signature;
    }

    @Serialize
    public String getHash() {
        return this.Hash;
    }

    public void setHash(String hash) {
        this.Hash = hash;
    }

    @Serialize
    public BigInteger getXAxis() {
        return this.XAxis;
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


    @Serialize
    @SerializeNullable
    public TransactionCallback getTransactionCallback() {
        return transactionCallback;
    }

    public void setTransactionCallback(TransactionCallback transactionCallback) {
        this.transactionCallback = transactionCallback;
    }

    public void infos(String value) {
        if (this.transactionCallback == null)
            return;
        this.transactionCallback.call(value);
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
        return ZoneFrom == that.ZoneFrom && ZoneTo == that.ZoneTo && BlockNumber == that.BlockNumber && Nonce == that.Nonce && Objects.equals(Hash, that.Hash) && Type == that.Type && Status == that.Status && Objects.equals(timestamp, that.timestamp) && Objects.equals(From, that.From) && Objects.equals(To, that.To) && Objects.equals(Amount, that.Amount) && Objects.equals(AmountWithTransactionFee, that.AmountWithTransactionFee) && Objects.equals(XAxis, that.XAxis) && Objects.equals(YAxis, that.YAxis) && Objects.equals(Signature, that.Signature);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Hash, Type, Status, ZoneFrom, ZoneTo, timestamp, BlockNumber, From, To, Amount, AmountWithTransactionFee, Nonce, XAxis, YAxis, Signature, transactionCallback);
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
                ", transactionCallback=" + transactionCallback +
                '}';
    }
}
