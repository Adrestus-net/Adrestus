package io.Adrestus.core;

public class UnclaimedFeeRewardTransaction extends RewardsTransaction{

    public UnclaimedFeeRewardTransaction(TransactionType type, String from, double amount) {
        super(type, from, amount);
    }


    @Override
    public double getAmountWithTransactionFee() {
        return super.getAmountWithTransactionFee();
    }

    @Override
    public void setAmount(double amount) {
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
