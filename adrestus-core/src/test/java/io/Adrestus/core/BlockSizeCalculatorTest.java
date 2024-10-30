package io.Adrestus.core;

import io.Adrestus.core.Util.BlockSizeCalculator;
import io.Adrestus.crypto.bls.BLSSignatureData;
import io.Adrestus.crypto.bls.model.BLSPrivateKey;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.elliptic.mapper.StakingData;
import io.Adrestus.p2p.kademlia.repository.KademliaData;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BlockSizeCalculatorTest {

    @Test
    public void TransactionBlockTest1() {
        TransactionBlock b = new TransactionBlock();
        b.setHeader(new AbstractBlock.Header(0, "", ""));
        b.setStatustype(StatusType.PENDING);
        b.setHash("hash");
        b.setSize(9);
        b.setHeight(100);
        b.setGeneration(2);
        b.setViewID(13);
        b.getSignatureData().put(new BLSPublicKey(), new BLSSignatureData());
        b.setZone(0);
        b.getTransactionList().add(new RegularTransaction("1"));
        LinkedHashMap<Receipt.ReceiptBlock, List<Receipt>> map = new LinkedHashMap<>();
        ArrayList<Receipt> list = new ArrayList<>();
        list.add(new Receipt());
        list.add(new Receipt());
        list.add(new Receipt());

        ArrayList<Receipt> list2 = new ArrayList<>();
        list2.add(new Receipt());
        list2.add(new Receipt());
        list2.add(new Receipt());

        Receipt.ReceiptBlock receipt2 = new Receipt.ReceiptBlock();
        receipt2.setHeight(2);
        map.put(new Receipt.ReceiptBlock(), list);
        map.put(receipt2, list2);
        b.getInbound().getMap_receipts().put(1, map);
        BlockSizeCalculator blockSizeCalculator = new BlockSizeCalculator(b);
        assertEquals(14336, blockSizeCalculator.TransactionBlockSizeCalculator());
    }

    @Test
    public void TransactionBlockTests2() {
        TransactionBlock b = new TransactionBlock();
        b.setHeader(new AbstractBlock.Header(0, "", ""));
        b.setStatustype(StatusType.PENDING);
        b.setHash("hash");
        b.setSize(9);
        b.setHeight(100);
        b.setGeneration(2);
        b.setViewID(13);
        b.getSignatureData().put(new BLSPublicKey(), new BLSSignatureData());
        b.setZone(0);
        b.getTransactionList().add(new RegularTransaction("1"));
        LinkedHashMap<Receipt.ReceiptBlock, List<Receipt>> map = new LinkedHashMap<>();
        LinkedHashMap<Receipt.ReceiptBlock, List<Receipt>> out_map = new LinkedHashMap<>();
        ArrayList<Receipt> list = new ArrayList<>();
        list.add(new Receipt());
        list.add(new Receipt());
        list.add(new Receipt());

        ArrayList<Receipt> list2 = new ArrayList<>();
        list2.add(new Receipt());
        list2.add(new Receipt());
        list2.add(new Receipt());

        ArrayList<Receipt> list3 = new ArrayList<>();
        list3.add(new Receipt());
        list3.add(new Receipt());
        list3.add(new Receipt());

        ArrayList<Receipt> list4 = new ArrayList<>();
        list4.add(new Receipt());
        list4.add(new Receipt());
        list4.add(new Receipt());

        Receipt.ReceiptBlock receipt2 = new Receipt.ReceiptBlock();
        receipt2.setHeight(2);

        Receipt.ReceiptBlock receipt3 = new Receipt.ReceiptBlock();
        receipt3.setHeight(2);

        Receipt.ReceiptBlock receipt4 = new Receipt.ReceiptBlock();
        receipt4.setHeight(2);


        map.put(new Receipt.ReceiptBlock(), list);
        map.put(receipt2, list2);

        out_map.put(receipt3, list3);
        out_map.put(receipt4, list4);
        b.getInbound().getMap_receipts().put(1, map);
        b.getOutbound().getMap_receipts().put(0, out_map);
        BlockSizeCalculator blockSizeCalculator = new BlockSizeCalculator(b);
        assertEquals(18432, blockSizeCalculator.TransactionBlockSizeCalculator());
    }

    @Test
    public void TransactionBlockTests3() {
        TransactionBlock b = new TransactionBlock();
        b.setHeader(new AbstractBlock.Header(0, "", ""));
        b.setStatustype(StatusType.PENDING);
        b.setHash("hash");
        b.setSize(9);
        b.setHeight(100);
        b.setGeneration(2);
        b.setViewID(13);
        b.getSignatureData().put(new BLSPublicKey(), new BLSSignatureData());
        b.setZone(0);
        b.getTransactionList().add(new RegularTransaction("1"));
        LinkedHashMap<Receipt.ReceiptBlock, List<Receipt>> map1 = new LinkedHashMap<>();
        LinkedHashMap<Receipt.ReceiptBlock, List<Receipt>> map2 = new LinkedHashMap<>();

        ArrayList<Receipt> list = new ArrayList<>();
        list.add(new Receipt());
        list.add(new Receipt());
        list.add(new Receipt());
        map1.put(new Receipt.ReceiptBlock(), list);
        map2.put(new Receipt.ReceiptBlock(), list);
        b.getInbound().getMap_receipts().put(0, map1);
        b.getInbound().getMap_receipts().put(1, map2);
        BlockSizeCalculator blockSizeCalculator = new BlockSizeCalculator(b);
        assertEquals(14336, blockSizeCalculator.TransactionBlockSizeCalculator());
    }

    @Test
    public void CommitteeBlockTests1() {
        CommitteeBlock b = new CommitteeBlock();
        b.setHeader(new AbstractBlock.Header(0, "", ""));
        b.setStatustype(StatusType.PENDING);
        b.setHash("hash");
        b.setSize(9);
        b.setHeight(100);
        b.setGeneration(2);
        b.setViewID(13);
        b.getSignatureData().put(new BLSPublicKey(), new BLSSignatureData());
        b.setCommitteeProposer(new int[5]);
        b.setVDF("vdf");
        b.setVRF("vrf");
        b.getStakingMap().put(new StakingData(1, BigDecimal.valueOf(2)), new KademliaData());
        b.getStakingMap().put(new StakingData(2, BigDecimal.valueOf(3)), new KademliaData());

        BlockSizeCalculator blockSizeCalculator = new BlockSizeCalculator(b);
        assertEquals(11284, blockSizeCalculator.CommitteeBlockSizeCalculator());
    }

    @Test
    public void CommitteeBlockTests2() {
        CommitteeBlock b = new CommitteeBlock();
        b.setHeader(new AbstractBlock.Header(0, "", ""));
        b.setStatustype(StatusType.PENDING);
        b.setHash("hash");
        b.setSize(9);
        b.setHeight(100);
        b.setGeneration(2);
        b.setViewID(13);
        b.getSignatureData().put(new BLSPublicKey(), new BLSSignatureData());
        b.setCommitteeProposer(new int[5]);
        b.setVDF("vdf");
        b.setVRF("vrf");
        b.getStakingMap().put(new StakingData(1, BigDecimal.valueOf(2)), new KademliaData());
        b.getStakingMap().put(new StakingData(2, BigDecimal.valueOf(3)), new KademliaData());

        b.getStructureMap().get(0).put(new BLSPublicKey(), new String("1"));
        b.getStructureMap().get(0).put(new BLSPublicKey(new BLSPrivateKey(2)), new String("2"));
        b.getStructureMap().get(1).put(new BLSPublicKey(), new String("1"));
        BlockSizeCalculator blockSizeCalculator = new BlockSizeCalculator(b);

        assertEquals(12308, blockSizeCalculator.CommitteeBlockSizeCalculator());
    }
}
