package io.Adrestus.core;

import io.Adrestus.crypto.WalletAddress;
import io.Adrestus.crypto.elliptic.ECKeyPair;
import io.Adrestus.crypto.elliptic.Keys;
import io.Adrestus.crypto.mnemonic.Mnemonic;
import io.Adrestus.crypto.mnemonic.MnemonicException;
import io.Adrestus.crypto.mnemonic.Security;
import io.Adrestus.crypto.mnemonic.WordList;
import io.Adrestus.util.SerializationUtil;
import org.junit.jupiter.api.Test;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.ArrayList;

public class TransactionTest {

    //@Test
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

    // @Test
    public void StakingTransaction_test() {
        SerializationUtil<Transaction> serenc = new SerializationUtil<Transaction>(Transaction.class);
        Transaction stakingTransaction = new StakingTransaction();
        stakingTransaction.setAmount(100);
        stakingTransaction.setType(TransactionType.STAKING);
        byte[] buffer = serenc.encode(stakingTransaction);

        Transaction copys = serenc.decode(buffer);
        System.out.println(copys.toString());
    }

    // @Test
    public void RewardTransaction_test() {
        SerializationUtil<Transaction> serenc = new SerializationUtil<Transaction>(Transaction.class);
        RewardsTransaction rewardsTransaction = new RewardsTransaction("Del");
        rewardsTransaction.setAmount(100);
        rewardsTransaction.setType(TransactionType.REWARDS);
        byte[] buffers = serenc.encode(rewardsTransaction);

        Transaction copys = serenc.decode(buffers);
        System.out.println(copys.toString());
    }

    // @Test
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


    @Test
    public void StressTesting() throws MnemonicException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        ArrayList<String> list = new ArrayList<>();
        int version = 0x00;
        int size = 100;
        for (int i = 0; i < size; i++) {
            Mnemonic mnem = new Mnemonic(Security.NORMAL, WordList.ENGLISH);
            char[] mnemonic_sequence = mnem.create();
            char[] passphrase = "p4ssphr4se".toCharArray();
            byte[] key = mnem.createSeed(mnemonic_sequence, passphrase);
            ECKeyPair ecKeyPair = Keys.createEcKeyPair(new SecureRandom(key));
            String adddress = WalletAddress.generate_address((byte) version, ecKeyPair.getPublicKey());
            list.add(adddress);
        }

        for (int i = 0; i < size - 1; i++) {

        }
    }

}
