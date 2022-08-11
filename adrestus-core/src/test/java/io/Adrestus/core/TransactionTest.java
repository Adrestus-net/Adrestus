package io.Adrestus.core;

import io.Adrestus.util.SerializationUtil;
import org.junit.jupiter.api.Test;

public class TransactionTest {

    @Test
    public void Transaction_test() {

        SerializationUtil<Transaction> ser = new SerializationUtil<Transaction>(Transaction.class);
        Transaction transaction = new RegularTransaction();
        transaction.setAmount(100);
        transaction.setHash("Hash");
        transaction.setType(TransactionType.REGULAR);
        byte[] buffer = ser.encode(transaction);
        Transaction copy = ser.decode(buffer);
        System.out.println(copy.toString());

    }

    @Test
    public void StakingTransaction_test() {
        SerializationUtil<Transaction> serenc = new SerializationUtil<Transaction>(Transaction.class);
        Transaction stakingTransaction = new StakingTransaction();
        stakingTransaction.setAmount(100);
        stakingTransaction.setType(TransactionType.STAKING);
        byte[] buffer = serenc.encode(stakingTransaction);

        Transaction copys = serenc.decode(buffer);
        System.out.println(copys.toString());
    }

    @Test
    public void RewardTransaction_test() {
        SerializationUtil<Transaction> serenc = new SerializationUtil<Transaction>(Transaction.class);
        RewardsTransaction rewardsTransaction = new RewardsTransaction("Del");
        rewardsTransaction.setAmount(100);
        rewardsTransaction.setType(TransactionType.REWARDS);
        byte[] buffers = serenc.encode(rewardsTransaction);

        Transaction copys = serenc.decode(buffers);
        System.out.println(copys.toString());
    }

    @Test
    public void DelegateTransaction_test() {
        SerializationUtil<Transaction> serenc = new SerializationUtil<Transaction>(Transaction.class);
        //byte[] buffer = new byte[200];
        // BinarySerializer<DelegateTransaction> serenc = SerializerBuilder.create().build(DelegateTransaction.class);
        Transaction delegateTransaction = new DelegateTransaction();
        delegateTransaction.setAmount(100);
        delegateTransaction.setType(TransactionType.DELEGATING);
        byte[] buffer = serenc.encode(delegateTransaction);

        Transaction copys = serenc.decode(buffer);
        System.out.println(copys.toString());

    }


}
