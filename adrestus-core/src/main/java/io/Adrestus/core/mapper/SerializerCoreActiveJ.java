package io.Adrestus.core.mapper;

import io.Adrestus.core.AbstractBlock;
import io.Adrestus.core.Transaction;
import io.Adrestus.crypto.bls.BLS381.ECP;
import io.Adrestus.crypto.bls.BLS381.ECP2;
import io.Adrestus.crypto.bls.mapper.ECP2mapper;
import io.Adrestus.crypto.bls.mapper.ECPmapper;
import io.Adrestus.crypto.elliptic.mapper.BigDecimalSerializer;
import io.Adrestus.crypto.elliptic.mapper.BigIntegerSerializer;
import io.Adrestus.util.SerializationUtil;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class SerializerCoreActiveJ {
    private static volatile SerializerCoreActiveJ instance;
    private final List<SerializationUtil.Mapping> list;
    private static volatile SerializationUtil<Transaction> transactionSerializationUtil;
    private static volatile SerializationUtil<AbstractBlock> blockSerializationUtil;

    private SerializerCoreActiveJ() {
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        }
        list = new ArrayList<>();
        list.add(new SerializationUtil.Mapping(BigDecimal.class, ctx -> new BigDecimalSerializer()));
        list.add(new SerializationUtil.Mapping(ECP.class, ctx -> new ECPmapper()));
        list.add(new SerializationUtil.Mapping(ECP2.class, ctx -> new ECP2mapper()));
        list.add(new SerializationUtil.Mapping(BigInteger.class, ctx -> new BigIntegerSerializer()));
        list.add(new SerializationUtil.Mapping(TreeMap.class, ctx -> new CustomSerializerTreeMap()));
        blockSerializationUtil = new SerializationUtil<AbstractBlock>(AbstractBlock.class, list);
        transactionSerializationUtil = new SerializationUtil<Transaction>(Transaction.class, list);
    }

    public static SerializerCoreActiveJ getInstance() {

        var result = instance;
        if (result == null) {
            synchronized (SerializerCoreActiveJ.class) {
                result = instance;
                if (result == null) {
                    result = new SerializerCoreActiveJ();
                    instance = result;
                }
            }
        }
        return result;
    }

    public synchronized SerializationUtil<AbstractBlock> getBlockSerializationUtil() {
        return blockSerializationUtil;
    }

    public synchronized SerializationUtil<Transaction> getTransactionSerializationUtil() {
        return transactionSerializationUtil;
    }
}
