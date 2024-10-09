package io.Adrestus.core;

import com.google.common.reflect.TypeToken;
import io.Adrestus.crypto.elliptic.mapper.BigDecimalSerializer;
import io.Adrestus.crypto.elliptic.mapper.BigIntegerSerializer;
import io.Adrestus.util.SerializationUtil;
import io.distributedLedger.DatabaseFactory;
import io.distributedLedger.DatabaseType;
import io.distributedLedger.IDatabase;
import io.distributedLedger.LevelDBReceiptWrapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LevelDBReceiptTest {


    @Test
    public void serialize() {
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(BigDecimal.class, ctx -> new BigDecimalSerializer()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        SerializationUtil<Receipt> recep = new SerializationUtil<Receipt>(Receipt.class, list);

        Transaction transaction = new RegularTransaction();
        transaction.setAmount(BigDecimal.valueOf(100));
        transaction.setHash("Hash");
        transaction.setFrom("1");
        transaction.setTo("2");

        Receipt receipt = new Receipt(0, 2);
        receipt.setReceiptBlock(new Receipt.ReceiptBlock(1, 1, "root"));
        byte[] buff = recep.encode(receipt);
        Receipt cloned = recep.decode(buff);
        assertEquals(receipt, cloned);
    }

    //@Test
    public void ReceiptTest() {
        IDatabase<String, LevelDBReceiptWrapper<Receipt>> receiptdatabase = new DatabaseFactory(String.class, Receipt.class, new TypeToken<LevelDBReceiptWrapper<Receipt>>() {
        }.getType()).getDatabase(DatabaseType.LEVEL_DB);
        Transaction transaction = new RegularTransaction();
        transaction.setAmount(BigDecimal.valueOf(100));
        transaction.setHash("Hash");
        transaction.setFrom("1");
        transaction.setTo("2");

        Receipt receipt = new Receipt(0, 2);
        receipt.setPosition(1);
        receipt.setReceiptBlock(new Receipt.ReceiptBlock(1, 1, "root"));
        receiptdatabase.save(transaction.getFrom(), receipt);
        Optional<LevelDBReceiptWrapper<Receipt>> wrapperreceipt = receiptdatabase.findByKey("1");
        assertEquals(receipt, wrapperreceipt.get().getTo().get(0));

        receiptdatabase.delete_db();
    }

    //@Test
    public void ReceiptTest2() {
        IDatabase<String, LevelDBReceiptWrapper<Receipt>> receiptdatabase = new DatabaseFactory(String.class, Receipt.class, new TypeToken<LevelDBReceiptWrapper<Receipt>>() {
        }.getType()).getDatabase(DatabaseType.LEVEL_DB);
        Transaction transaction = new RegularTransaction();
        transaction.setAmount(BigDecimal.valueOf(100));
        transaction.setHash("Hash");
        transaction.setFrom("1");
        transaction.setTo("2");

        Transaction transaction2 = new RegularTransaction();
        transaction2.setAmount(BigDecimal.valueOf(100));
        transaction2.setHash("Hash2");
        transaction2.setFrom("1");
        transaction2.setTo("2");

        Receipt receipt = new Receipt(0, 2);
        receipt.setPosition(1);
        receipt.setReceiptBlock(new Receipt.ReceiptBlock(1, 1, "root"));
        Receipt receipt2 = new Receipt(0, 2);
        receipt2.setPosition(2);
        receipt2.setReceiptBlock(new Receipt.ReceiptBlock(2, 1, "root"));
        receiptdatabase.save(transaction.getFrom(), receipt);
        receiptdatabase.save(transaction2.getFrom(), receipt2);
        receiptdatabase.save(transaction2.getFrom(), receipt2);
        receiptdatabase.save(transaction2.getFrom(), receipt2);
        Optional<LevelDBReceiptWrapper<Receipt>> wrapperreceipt = receiptdatabase.findByKey("1");
        assertEquals(receipt, wrapperreceipt.get().getTo().get(0));
        assertEquals(receipt2, wrapperreceipt.get().getTo().get(1));
        assertEquals(2, wrapperreceipt.get().getTo().size());
        receiptdatabase.delete_db();
    }
}
