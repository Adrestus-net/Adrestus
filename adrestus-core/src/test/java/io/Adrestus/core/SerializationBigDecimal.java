package io.Adrestus.core;

import io.Adrestus.core.Util.BlockSizeCalculator;
import io.Adrestus.crypto.bls.BLS381.ECP;
import io.Adrestus.crypto.bls.BLS381.ECP2;
import io.Adrestus.crypto.bls.mapper.ECP2mapper;
import io.Adrestus.crypto.bls.mapper.ECPmapper;
import io.Adrestus.crypto.elliptic.mapper.BigDecimalSerializer;
import io.Adrestus.crypto.elliptic.mapper.BigIntegerSerializer;
import io.Adrestus.crypto.elliptic.mapper.CustomSerializerTreeMap;
import io.Adrestus.util.SerializationUtil;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SerializationBigDecimal {
    private static SerializationUtil<AbstractBlock> serenc;
    private static BlockSizeCalculator sizeCalculator;

    @SneakyThrows
    @BeforeAll
    public static void setup() {
        sizeCalculator = new BlockSizeCalculator();
        List<SerializationUtil.Mapping> list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
        list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        list.add(new SerializationUtil.Mapping(BigDecimal.class, ctx -> new BigDecimalSerializer()));
        list.add(new SerializationUtil.Mapping(TreeMap.class, ctx -> new CustomSerializerTreeMap()));
        serenc = new SerializationUtil<AbstractBlock>(AbstractBlock.class, list);

    }

    @Test
    public void SerializeBigDecimal() {
        BigInteger bigInteger = new BigInteger("23");
        BigDecimal bigDecimal = new BigDecimal(bigInteger, 2);
        TransactionBlock block = new TransactionBlock();
        Transaction transaction1 = new RegularTransaction();
        transaction1.setAmount(BigDecimal.valueOf(12.456823));
        transaction1.setAmountWithTransactionFee(BigDecimal.valueOf(12.456823));
        Transaction transaction2 = new RegularTransaction();
        transaction2.setAmount(BigDecimal.valueOf(12.4568));
        transaction2.setAmountWithTransactionFee(BigDecimal.valueOf(0.30));
        Transaction transaction3 = new RegularTransaction();
        transaction3.setAmount(BigDecimal.valueOf(1245.4568));
        transaction3.setAmountWithTransactionFee(BigDecimal.valueOf(1245.4568));
        Transaction transaction4 = new RegularTransaction();
        transaction4.setAmount(BigDecimal.valueOf(1.223432));
        transaction4.setAmountWithTransactionFee(BigDecimal.valueOf(1.2).setScale(1, BigDecimal.ROUND_HALF_UP));
        Transaction transaction5 = new RegularTransaction();
        transaction5.setAmount(BigDecimal.valueOf(1));
        transaction5.setAmountWithTransactionFee(BigDecimal.valueOf(12));
        Transaction transaction6 = new RegularTransaction();
        transaction6.setAmount(new BigDecimal(0.2).setScale(1, BigDecimal.ROUND_HALF_UP));
        transaction6.setAmountWithTransactionFee(new BigDecimal(0.233232).setScale(6, BigDecimal.ROUND_HALF_UP));
        Transaction transaction7 = new RegularTransaction();
        transaction7.setAmount(new BigDecimal(0.2465).setScale(4, BigDecimal.ROUND_HALF_UP));
        transaction7.setAmountWithTransactionFee(new BigDecimal(5464));
        block.getTransactionList().add(transaction1);
        block.getTransactionList().add(transaction2);
        block.getTransactionList().add(transaction3);
        block.getTransactionList().add(transaction4);
        block.getTransactionList().add(transaction5);
        block.getTransactionList().add(transaction6);
        block.getTransactionList().add(transaction7);
        this.sizeCalculator.setTransactionBlock(block);
        byte[] message = serenc.encode(block, this.sizeCalculator.TransactionBlockSizeCalculator());
        TransactionBlock clone = (TransactionBlock) serenc.decode(message);

        assertEquals(12.456823234234, BigDecimal.valueOf(12.456823234234).doubleValue());
        assertEquals(12.456823, block.getTransactionList().get(0).getAmount().doubleValue());
        assertEquals(12.456823, block.getTransactionList().get(0).getAmountWithTransactionFee().doubleValue());
        assertEquals(12.4568, block.getTransactionList().get(1).getAmount().doubleValue());
        assertEquals(0.30, block.getTransactionList().get(1).getAmountWithTransactionFee().doubleValue());
        assertEquals(1245.4568, block.getTransactionList().get(2).getAmount().doubleValue());
        assertEquals(1245.4568, block.getTransactionList().get(2).getAmountWithTransactionFee().doubleValue());
        assertEquals(1.223432, block.getTransactionList().get(3).getAmount().doubleValue());
        assertEquals(1.2, block.getTransactionList().get(3).getAmountWithTransactionFee().doubleValue());
        assertEquals(1, block.getTransactionList().get(4).getAmount().doubleValue());
        assertEquals(12, block.getTransactionList().get(4).getAmountWithTransactionFee().doubleValue());
        assertEquals(0.2, block.getTransactionList().get(5).getAmount().doubleValue());
        assertEquals(0.233232, block.getTransactionList().get(5).getAmountWithTransactionFee().doubleValue());
        assertEquals(0.2465, block.getTransactionList().get(6).getAmount().doubleValue());
        assertEquals(5464, block.getTransactionList().get(6).getAmountWithTransactionFee().doubleValue());

        assertEquals(12.456823234234, BigDecimal.valueOf(12.456823234234).doubleValue());
        assertEquals(12.456823,  clone.getTransactionList().get(0).getAmount().doubleValue());
        assertEquals(12.456823,  clone.getTransactionList().get(0).getAmountWithTransactionFee().doubleValue());
        assertEquals(12.4568,  clone.getTransactionList().get(1).getAmount().doubleValue());
        assertEquals(0.30, block.getTransactionList().get(1).getAmountWithTransactionFee().doubleValue());
        assertEquals(1245.4568,  clone.getTransactionList().get(2).getAmount().doubleValue());
        assertEquals(1245.4568,  clone.getTransactionList().get(2).getAmountWithTransactionFee().doubleValue());
        assertEquals(1.223432,  clone.getTransactionList().get(3).getAmount().doubleValue());
        assertEquals(1.2,  clone.getTransactionList().get(3).getAmountWithTransactionFee().doubleValue());
        assertEquals(1,  clone.getTransactionList().get(4).getAmount().doubleValue());
        assertEquals(12,  clone.getTransactionList().get(4).getAmountWithTransactionFee().doubleValue());
        assertEquals(0.2,  clone.getTransactionList().get(5).getAmount().doubleValue());
        assertEquals(0.233232,  clone.getTransactionList().get(5).getAmountWithTransactionFee().doubleValue());
        assertEquals(0.2465,  clone.getTransactionList().get(6).getAmount().doubleValue());
        assertEquals(5464,  clone.getTransactionList().get(6).getAmountWithTransactionFee().doubleValue());
        assertEquals(block, clone);

    }
}
