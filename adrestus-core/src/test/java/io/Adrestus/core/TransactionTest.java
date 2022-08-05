package io.Adrestus.core;

import io.Adrestus.util.SerializationUtil;
import org.junit.jupiter.api.Test;

public class TransactionTest {

    @Test
    public void Transaction_test() {

        SerializationUtil<Transaction> ser = new SerializationUtil<Transaction>(Transaction.class);
        Transaction transaction = new Transaction();
        transaction.setAmount(100);
        transaction.setHash("Hash");
        byte[] buffer = ser.encode(transaction);
        Transaction copy = ser.decode(buffer);
        System.out.println(copy.toString());

    }

    @Test
    public void StakingTransaction_test() {

    }

    @Test
    public void RewardTransaction_test() {
        SerializationUtil<ToSend> serenc = new SerializationUtil<ToSend>(ToSend.class);
        RewardsTransaction rewardsTransaction = new RewardsTransaction("Del");
        //rewardsTransaction.setDelegatorAddress("Del");
        rewardsTransaction.setAmount(100);
        rewardsTransaction.setType(TransactionType.REWARDS);
        ToSend sen = new ToSend(rewardsTransaction);
        //sen.setTransaction(rewardsTransaction);
        // System.out.println(sen.getTransaction().toString());
        byte[] buffers = serenc.encode(sen);


        SerializationUtil<ToSend> serdec = new SerializationUtil<ToSend>(ToSend.class);
        ToSend copys = serenc.decode(buffers);
        System.out.println(copys.getTransaction().toString());
    }

    @Test
    public void DelegateTransaction_test() {

    }


}
