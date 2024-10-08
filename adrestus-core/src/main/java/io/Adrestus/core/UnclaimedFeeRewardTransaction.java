package io.Adrestus.core;

import java.math.BigDecimal;

public class UnclaimedFeeRewardTransaction extends RewardsTransaction {


    public UnclaimedFeeRewardTransaction() {
        super();
    }

    public UnclaimedFeeRewardTransaction(TransactionType type, String from, BigDecimal amount) {
        super(type, from, amount);
    }


    @Override
    public void accept(TransactionUnitVisitor visitor) {
        visitor.visit(this);
    }


    @Override
    public BigDecimal getAmountWithTransactionFee() {
        return super.getAmountWithTransactionFee();
    }

    @Override
    public void setAmount(BigDecimal amount) {
        super.setAmount(amount);
    }

    @Override
    public String getRecipientAddress() {
        return super.getRecipientAddress();
    }

    @Override
    public void setRecipientAddress(String RecipientAddress) {
        super.setRecipientAddress(RecipientAddress);
    }

    @Override
    public void setType(TransactionType type) {
        super.setType(type);
    }


    @Override
    public TransactionType getType() {
        return super.getType();
    }

    @Override
    public void infos(String value) {
        if (this.transactionCallback == null)
            return;

        this.transactionCallback.call(value);
    }


    @Override
    public String toString() {
        return "UnclaimedFeedRewardTransaction{" +
                "Signature=" + Signature +
                ", YAxis=" + YAxis +
                ", XAxis=" + XAxis +
                ", Nonce=" + Nonce +
                ", AmountWithTransactionFee=" + AmountWithTransactionFee +
                ", Amount=" + Amount +
                ", To='" + To + '\'' +
                ", From='" + From + '\'' +
                ", BlockNumber=" + BlockNumber +
                ", timestamp='" + timestamp + '\'' +
                ", ZoneTo=" + ZoneTo +
                ", ZoneFrom=" + ZoneFrom +
                ", Status=" + Status +
                ", Type=" + Type +
                ", Hash='" + Hash + '\'' +
                '}';
    }
}
