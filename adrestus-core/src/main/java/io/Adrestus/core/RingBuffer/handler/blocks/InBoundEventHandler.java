package io.Adrestus.core.RingBuffer.handler.blocks;

import io.Adrestus.Trie.MerkleNode;
import io.Adrestus.Trie.MerkleTreeImp;
import io.Adrestus.config.NetworkConfiguration;
import io.Adrestus.core.*;
import io.Adrestus.core.Resourses.CachedEventLoop;
import io.Adrestus.core.Resourses.CachedLatestBlocks;
import io.Adrestus.core.Resourses.CachedZoneIndex;
import io.Adrestus.core.RingBuffer.event.AbstractBlockEvent;
import io.Adrestus.crypto.bls.model.CachedBLSKeyPair;
import io.Adrestus.rpc.RpcAdrestusClient;
import io.activej.eventloop.Eventloop;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class InBoundEventHandler implements BlockEventHandler<AbstractBlockEvent> {
    private static Logger LOG = LoggerFactory.getLogger(InBoundEventHandler.class);
    private final IBlockIndex blockIndex;

    public InBoundEventHandler() {
        this.blockIndex=new BlockIndex();
    }

    @Override
    public void onEvent(AbstractBlockEvent blockEvent, long l, boolean b) throws InterruptedException {
        TransactionBlock transactionBlock = (TransactionBlock) blockEvent.getBlock();
        CommitteeBlock committeeBlock = CachedLatestBlocks.getInstance().getCommitteeBlock();
        final Map<Integer, Map<Receipt.ReceiptBlock, List<Receipt>>> inner_receipts = transactionBlock.getInbound().getMap_receipts();

        if (transactionBlock.getInbound() == null)
            return;

        if (inner_receipts.size() > 3) {
            LOG.info("Size of zone is invalid");
            transactionBlock.setStatustype(StatusType.ABORT);
            return;
        }
        for (Integer key : inner_receipts.keySet()) {
            if (key == CachedZoneIndex.getInstance().getZoneIndex()) {
                LOG.info("Sender zone is invalid");
                transactionBlock.setStatustype(StatusType.ABORT);
                return;
            }
        }

        Collections.sort(transactionBlock.getTransactionList());
        ExecutorService service = Executors.newFixedThreadPool(3);
        Set<Integer> keyset = inner_receipts.keySet();


        AtomicInteger atomicInteger = new AtomicInteger(3);
        CountDownLatch latch = new CountDownLatch(3);

        try {
            Map<Receipt.ReceiptBlock, List<Receipt>> zone_1 = inner_receipts.get(keyset.toArray()[0]);
            CountDownLatch inner_latch = new CountDownLatch(1);
            AtomicReference<List<AbstractBlock>> atomicReference = new AtomicReference<>(new ArrayList<>());
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        //find validator position in structure map
                        Integer my_pos = blockIndex.getPublicKeyIndex(CachedZoneIndex.getInstance().getZoneIndex(), CachedBLSKeyPair.getInstance().getPublicKey());
                        // get first zone index from inner receipts and search in foor loop in which zone index of structure map belongs
                        Integer receiptZoneIndex = inner_receipts.keySet().stream().findFirst().get();
                        String IP = "";
                        for (Integer BlockZoneIndex : committeeBlock.getStructureMap().keySet()) {
                            if (BlockZoneIndex == receiptZoneIndex) {

                                //Fill in the list with auto increment positions of Linkdhasamp ip where zone index belong
                                List<Integer> searchable_list = IntStream
                                        .range(0, 0 + committeeBlock.getStructureMap().get(BlockZoneIndex).size())
                                        .boxed().collect(Collectors.toList());

                                // Find the closest value of ip in order to get this ip from linkdnHashmap
                                //and look for value
                                int target = searchable_list.stream()
                                        .min(Comparator.comparingInt(i -> Math.abs(i - my_pos)))
                                        .orElseThrow(() -> new NoSuchElementException("No value present"));
                                IP = blockIndex.getIpValue(BlockZoneIndex, blockIndex.getPublicKeyByIndex(BlockZoneIndex, target));
                                break;
                            }
                        }
                        if (IP.equals("")) {
                            LOG.info("Cross zone Verification failed not valid IP");
                            transactionBlock.setStatustype(StatusType.ABORT);
                            return;
                        }
                        ArrayList<String> to_search = new ArrayList<>();
                        for (Receipt.ReceiptBlock receiptBlock : zone_1.keySet()) {
                            to_search.add(receiptBlock.getBlock_hash());
                        }
                        RpcAdrestusClient<AbstractBlock> client = new RpcAdrestusClient<AbstractBlock>(new TransactionBlock(), IP, NetworkConfiguration.RPC_PORT, CachedEventLoop.getInstance().getEventloop());
                        client.connect();
                        List<AbstractBlock> current = client.getBlock(to_search);
                        if (current!=null)
                            atomicReference.set(client.getBlock(to_search));

                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        inner_latch.countDown();
                    }
                }
            }).start();
            service.submit(() -> {
                for (Map.Entry<Receipt.ReceiptBlock, List<Receipt>> entry : zone_1.entrySet()) {
                    entry.getValue().stream().forEach(receipt -> {
                        int index = Collections.binarySearch(transactionBlock.getTransactionList(), receipt.getTransaction());
                        Transaction transaction = transactionBlock.getTransactionList().get(index);
                        boolean check = PreConditionsChecks(receipt, entry.getKey(), transactionBlock, transaction, index);
                        if (!check)
                            atomicInteger.decrementAndGet();
                    });
                }
                try {
                    inner_latch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (atomicReference.get().isEmpty())
                    atomicInteger.decrementAndGet();

                else {
                    for (Map.Entry<Receipt.ReceiptBlock, List<Receipt>> entry : zone_1.entrySet()) {
                        for (int i = 0; i < atomicReference.get().size(); i++) {
                            boolean check = CrossZoneConditionsChecks((TransactionBlock) atomicReference.get().get(i), entry.getKey());
                            if (!check)
                                atomicInteger.decrementAndGet();
                        }
                    }
                }

                latch.countDown();

            });
        } catch (Exception e) {
           // e.printStackTrace();
            latch.countDown();
        }

        try {
            Map<Receipt.ReceiptBlock, List<Receipt>> zone_2 = inner_receipts.get(keyset.toArray()[1]);
            CountDownLatch inner_latch = new CountDownLatch(1);
            AtomicReference<List<AbstractBlock>> atomicReference = new AtomicReference<>(new ArrayList<>());
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        //find validator position in structure map
                        Integer my_pos = blockIndex.getPublicKeyIndex(CachedZoneIndex.getInstance().getZoneIndex(), CachedBLSKeyPair.getInstance().getPublicKey());
                        // get first zone index from inner receipts and search in foor loop in which zone index of structure map belongs
                        Integer receiptZoneIndex = inner_receipts.keySet().stream().findFirst().get();
                        String IP = "";
                        for (Integer BlockZoneIndex : committeeBlock.getStructureMap().keySet()) {
                            if (BlockZoneIndex == receiptZoneIndex) {

                                //Fill in the list with auto increment positions of Linkdhasamp ip where zone index belong
                                List<Integer> searchable_list = IntStream
                                        .range(0, 0 + committeeBlock.getStructureMap().get(BlockZoneIndex).size())
                                        .boxed().collect(Collectors.toList());

                                // Find the closest value of ip in order to get this ip from linkdnHashmap
                                //and look for value
                                int target = searchable_list.stream()
                                        .min(Comparator.comparingInt(i -> Math.abs(i - my_pos)))
                                        .orElseThrow(() -> new NoSuchElementException("No value present"));
                                IP = blockIndex.getIpValue(BlockZoneIndex, blockIndex.getPublicKeyByIndex(BlockZoneIndex, target));
                                break;
                            }
                        }
                        if (IP.equals("")) {
                            LOG.info("Cross zone Verification failed not valid IP");
                            transactionBlock.setStatustype(StatusType.ABORT);
                            return;
                        }
                        ArrayList<String> to_search = new ArrayList<>();
                        for (Receipt.ReceiptBlock receiptBlock : zone_2.keySet()) {
                            to_search.add(receiptBlock.getBlock_hash());
                        }
                        RpcAdrestusClient<AbstractBlock> client = new RpcAdrestusClient<AbstractBlock>(new TransactionBlock(), IP, NetworkConfiguration.RPC_PORT, CachedEventLoop.getInstance().getEventloop());
                        client.connect();
                        List<AbstractBlock> current = client.getBlock(to_search);
                        if (current!=null)
                            atomicReference.set(client.getBlock(to_search));

                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        inner_latch.countDown();
                    }
                }
            }).start();
            service.submit(() -> {
                for (Map.Entry<Receipt.ReceiptBlock, List<Receipt>> entry : zone_2.entrySet()) {
                    entry.getValue().stream().forEach(receipt -> {
                        int index = Collections.binarySearch(transactionBlock.getTransactionList(), receipt.getTransaction());
                        Transaction transaction = transactionBlock.getTransactionList().get(index);
                        boolean check = PreConditionsChecks(receipt, entry.getKey(), transactionBlock, transaction, index);
                        if (!check)
                            atomicInteger.decrementAndGet();
                    });
                }
                try {
                    inner_latch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (atomicReference.get().isEmpty())
                    atomicInteger.decrementAndGet();

                else {
                    for (Map.Entry<Receipt.ReceiptBlock, List<Receipt>> entry : zone_2.entrySet()) {
                        for (int i = 0; i < atomicReference.get().size(); i++) {
                            boolean check = CrossZoneConditionsChecks((TransactionBlock) atomicReference.get().get(i), entry.getKey());
                            if (!check)
                                atomicInteger.decrementAndGet();
                        }
                    }
                }

                latch.countDown();

            });
        } catch (Exception e) {
            // e.printStackTrace();
            latch.countDown();
        }

        try {
            Map<Receipt.ReceiptBlock, List<Receipt>> zone_3= inner_receipts.get(keyset.toArray()[2]);
            CountDownLatch inner_latch = new CountDownLatch(1);
            AtomicReference<List<AbstractBlock>> atomicReference = new AtomicReference<>(new ArrayList<>());
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        //find validator position in structure map
                        Integer my_pos = blockIndex.getPublicKeyIndex(CachedZoneIndex.getInstance().getZoneIndex(), CachedBLSKeyPair.getInstance().getPublicKey());
                        // get first zone index from inner receipts and search in foor loop in which zone index of structure map belongs
                        Integer receiptZoneIndex = inner_receipts.keySet().stream().findFirst().get();
                        String IP = "";
                        for (Integer BlockZoneIndex : committeeBlock.getStructureMap().keySet()) {
                            if (BlockZoneIndex == receiptZoneIndex) {

                                //Fill in the list with auto increment positions of Linkdhasamp ip where zone index belong
                                List<Integer> searchable_list = IntStream
                                        .range(0, 0 + committeeBlock.getStructureMap().get(BlockZoneIndex).size())
                                        .boxed().collect(Collectors.toList());

                                // Find the closest value of ip in order to get this ip from linkdnHashmap
                                //and look for value
                                int target = searchable_list.stream()
                                        .min(Comparator.comparingInt(i -> Math.abs(i - my_pos)))
                                        .orElseThrow(() -> new NoSuchElementException("No value present"));
                                IP = blockIndex.getIpValue(BlockZoneIndex, blockIndex.getPublicKeyByIndex(BlockZoneIndex, target));
                                break;
                            }
                        }
                        if (IP.equals("")) {
                            LOG.info("Cross zone Verification failed not valid IP");
                            transactionBlock.setStatustype(StatusType.ABORT);
                            return;
                        }
                        ArrayList<String> to_search = new ArrayList<>();
                        for (Receipt.ReceiptBlock receiptBlock : zone_3.keySet()) {
                            to_search.add(receiptBlock.getBlock_hash());
                        }
                        RpcAdrestusClient<AbstractBlock> client = new RpcAdrestusClient<AbstractBlock>(new TransactionBlock(), IP, NetworkConfiguration.RPC_PORT, CachedEventLoop.getInstance().getEventloop());
                        client.connect();
                        List<AbstractBlock> current = client.getBlock(to_search);
                        if (current!=null)
                            atomicReference.set(client.getBlock(to_search));

                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        inner_latch.countDown();
                    }
                }
            }).start();
            service.submit(() -> {
                for (Map.Entry<Receipt.ReceiptBlock, List<Receipt>> entry : zone_3.entrySet()) {
                    entry.getValue().stream().forEach(receipt -> {
                        int index = Collections.binarySearch(transactionBlock.getTransactionList(), receipt.getTransaction());
                        Transaction transaction = transactionBlock.getTransactionList().get(index);
                        boolean check = PreConditionsChecks(receipt, entry.getKey(), transactionBlock, transaction, index);
                        if (!check)
                            atomicInteger.decrementAndGet();
                    });
                }
                try {
                    inner_latch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (atomicReference.get().isEmpty())
                    atomicInteger.decrementAndGet();

                else {
                    for (Map.Entry<Receipt.ReceiptBlock, List<Receipt>> entry : zone_3.entrySet()) {
                        for (int i = 0; i < atomicReference.get().size(); i++) {
                            boolean check = CrossZoneConditionsChecks((TransactionBlock) atomicReference.get().get(i), entry.getKey());
                            if (!check)
                                atomicInteger.decrementAndGet();
                        }
                    }
                }

                latch.countDown();

            });
        } catch (Exception e) {
            // e.printStackTrace();
            latch.countDown();
        }


        latch.await();
        service.shutdownNow();
        service = null;
        if (atomicInteger.get() != 3) {
            LOG.info("Validation check of Outbound list is invalid abort");
            transactionBlock.setStatustype(StatusType.ABORT);
            return;
        }
    }

    public boolean PreConditionsChecks(final Receipt receipt, final Receipt.ReceiptBlock receiptBlock, final TransactionBlock transactionBlock, Transaction transaction, int index) {
        final MerkleTreeImp outer_tree = new MerkleTreeImp();
        final ArrayList<MerkleNode> merkleNodeArrayList = new ArrayList<>();
        transactionBlock.getTransactionList().forEach(val -> merkleNodeArrayList.add(new MerkleNode(val.getHash())));
        outer_tree.my_generate2(merkleNodeArrayList);
        boolean bool1 = StringUtils.equals(transaction.getHash(), receipt.getTransaction().getHash());
        boolean bool2 = StringUtils.equals(transaction.getTo(), receipt.getAddress());
        boolean bool3 = StringUtils.equals(transactionBlock.getMerkleRoot(), outer_tree.GenerateRoot(receipt.getProofs()));
        boolean bool4 = StringUtils.equals(transactionBlock.getHash(), receiptBlock.getBlock_hash());
        boolean bool5 = StringUtils.equals(receiptBlock.getOutboundMerkleRoot(), outer_tree.getRootHash());
        double val2 = Double.compare(transaction.getAmount(), receipt.getAmount());
        int val3 = Integer.compare(index, receipt.getPosition());
        int val4 = Integer.compare(transactionBlock.getHeight(), receiptBlock.getHeight());
        int val5 = Integer.compare(transactionBlock.getGeneration(), receiptBlock.getGeneration());
        int val6 = Integer.compare(receipt.getZoneFrom(), CachedZoneIndex.getInstance().getZoneIndex());

        if (val2 == 0.0 || val3 == 0 || val4 == 0.0 || val5 == 0 || val6 == 0 || bool1 || bool2 || bool3 || bool4 || bool5)
            return true;
        return false;
    }

    public boolean CrossZoneConditionsChecks(final TransactionBlock transactionBlock, final Receipt.ReceiptBlock receiptBlock) {
        return StringUtils.equals(transactionBlock.getMerkleRoot(), receiptBlock.getOutboundMerkleRoot()) ? true : false;
    }
}
