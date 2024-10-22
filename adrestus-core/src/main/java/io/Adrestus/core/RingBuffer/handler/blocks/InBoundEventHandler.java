package io.Adrestus.core.RingBuffer.handler.blocks;

import io.Adrestus.Trie.MerkleNode;
import io.Adrestus.Trie.MerkleTreeOptimizedImp;
import io.Adrestus.core.*;
import io.Adrestus.core.Resourses.CachedInboundTransactionBlocks;
import io.Adrestus.core.Resourses.CachedLatestBlocks;
import io.Adrestus.core.Resourses.CachedZoneIndex;
import io.Adrestus.core.RingBuffer.event.AbstractBlockEvent;
import io.Adrestus.core.RingBuffer.publisher.ReceiptEventPublisher;
import io.Adrestus.crypto.bls.model.CachedBLSKeyPair;
import io.Adrestus.network.CachedEventLoop;
import io.Adrestus.rpc.RpcAdrestusClient;
import io.distributedLedger.ZoneDatabaseFactory;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class InBoundEventHandler implements BlockEventHandler<AbstractBlockEvent> {
    private static Logger LOG = LoggerFactory.getLogger(InBoundEventHandler.class);
    private static final int INBOUND_TIMEOUT = 400;

    private final IBlockIndex blockIndex;
    private final ReceiptEventPublisher publisher;
    private final RpcAdrestusClient<TransactionBlock> client_zone0, client_zone1, client_zone2, client_zone3;
    private TransactionBlock transactionBlock, transactionBlockClonable;
    private CommitteeBlock committeeBlock;
    private LinkedHashMap<Integer, LinkedHashMap<Receipt.ReceiptBlock, List<Receipt>>> inner_receipts;
    private AtomicInteger atomicInteger;
    private CountDownLatch latch;

    public InBoundEventHandler() {
        this.publisher = new ReceiptEventPublisher(1024);
        this.publisher.
                withGenerationEventHandler().
                withHeightEventHandler().
                withOutboundMerkleEventHandler().
                withZoneEventHandler().
                withReplayEventHandler().
                withEmptyEventHandler().
                withPublicKeyEventHandler()
                .withSignatureEventHandler()
                .withZoneFromEventHandler()
                .mergeEventsWithNoInsert();
        this.publisher.start();
        this.blockIndex = new BlockIndex();
        this.client_zone0 = new RpcAdrestusClient<TransactionBlock>(new TransactionBlock(), "", 0, INBOUND_TIMEOUT, CachedEventLoop.getInstance().getEventloop());
        this.client_zone1 = new RpcAdrestusClient<TransactionBlock>(new TransactionBlock(), "", 0, INBOUND_TIMEOUT, CachedEventLoop.getInstance().getEventloop());
        this.client_zone2 = new RpcAdrestusClient<TransactionBlock>(new TransactionBlock(), "", 0, INBOUND_TIMEOUT, CachedEventLoop.getInstance().getEventloop());
        this.client_zone3 = new RpcAdrestusClient<TransactionBlock>(new TransactionBlock(), "", 0, INBOUND_TIMEOUT, CachedEventLoop.getInstance().getEventloop());
    }

    @SneakyThrows
    @Override
    public void onEvent(AbstractBlockEvent blockEvent, long l, boolean b) throws InterruptedException {
        transactionBlock = (TransactionBlock) blockEvent.getBlock();
        transactionBlockClonable = (TransactionBlock) transactionBlock.clone();
        committeeBlock = CachedLatestBlocks.getInstance().getCommitteeBlock();
        inner_receipts = transactionBlockClonable.getInbound().getMap_receipts();

        if (transactionBlockClonable.getInbound().getMap_receipts().isEmpty())
            return;

        if (inner_receipts.size() > 3) {
            LOG.info("Size of zone is invalid");
            transactionBlock.setStatustype(StatusType.ABORT);
            return;
        }

        inner_receipts.values().forEach(val -> val.values().stream().forEach(col -> col.stream().forEach(rcp -> {
            if (rcp.getZoneTo() != CachedZoneIndex.getInstance().getZoneIndex()) {
                LOG.info("Sender zone is invalid");
                transactionBlock.setStatustype(StatusType.ABORT);
                return;
            }
        })));

        Collections.sort(transactionBlockClonable.getTransactionList());
        Set<Integer> keyset = inner_receipts.keySet();


        ExecutorService service = Executors.newFixedThreadPool(keyset.size());
        atomicInteger = new AtomicInteger(keyset.size());
        latch = new CountDownLatch(keyset.size());


        ExecutorService finalService = service;
        keyset.forEach(key -> {
            finalService.submit(() -> {
                try {
                    Map<Receipt.ReceiptBlock, List<Receipt>> zone = inner_receipts.get(key);
                    switch (key) {
                        case 0:
                            ServiceSubmit(client_zone0, key, zone);
                            break;
                        case 1:
                            ServiceSubmit(client_zone1, key, zone);
                        case 2:
                            ServiceSubmit(client_zone2, key, zone);
                        case 3:
                            ServiceSubmit(client_zone3, key, zone);
                        default:
                            ServiceSubmit(client_zone0, key, zone);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        });

        latch.await();
        service.shutdownNow();
        service = null;
        if (atomicInteger.get() != keyset.size()) {
            LOG.info("Validation check of Inbound list is invalid abort");
            transactionBlock.setStatustype(StatusType.ABORT);
            return;
        }

    }


    @SneakyThrows
    public void ServiceSubmit(RpcAdrestusClient client, int zoneIndex, Map<Receipt.ReceiptBlock, List<Receipt>> zone) {

        //find validator position in structure map
        Integer my_pos = blockIndex.getPublicKeyIndex(CachedZoneIndex.getInstance().getZoneIndex(), CachedBLSKeyPair.getInstance().getPublicKey());
        boolean bError = false;
        Integer receiptZoneIndex = 0;
        List<TransactionBlock> current_transaction_block = null;
        do {
            try {
                // get first zone index from inner receipts and search in foor loop in which zone index of structure map belongs
                receiptZoneIndex = inner_receipts.keySet().stream().findFirst().get();
                String IP = "";
                for (Integer BlockZoneIndex : committeeBlock.getStructureMap().keySet()) {
                    if (Objects.equals(BlockZoneIndex, receiptZoneIndex)) {

                        //Fill in the list with auto increment positions of Linkdhasamp ip where zone index belong
                        List<Integer> searchable_list = IntStream
                                .range(0, 0 + committeeBlock.getStructureMap().get(BlockZoneIndex).size())
                                .boxed().collect(Collectors.toList());

                        // Find the closest value of ip in order to get this ip from linkdnHashmap
                        //and look for value
                        Integer finalMy_pos = my_pos;
                        int target = searchable_list.stream()
                                .min(Comparator.comparingInt(i -> Math.abs(i - finalMy_pos)))
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
                for (Receipt.ReceiptBlock receiptBlock : zone.keySet()) {
                    to_search.add(String.valueOf(receiptBlock.getHeight()));
                }

                client.setHost(IP);
                client.setPort(ZoneDatabaseFactory.getDatabaseRPCPort(blockIndex.getZone(IP)));
                client.connect();

                current_transaction_block = client.getBlock(to_search);
                bError = false;
                HashMap<Integer, TransactionBlock> trxtosave = new HashMap<>();
                current_transaction_block.stream().forEach(val -> trxtosave.put(val.getHeight(), val));
                CachedInboundTransactionBlocks.getInstance().store(zoneIndex, trxtosave);
            } catch (IllegalArgumentException e) {
                bError = true;
                if (my_pos == committeeBlock.getStructureMap().get(receiptZoneIndex).size() - 1) {
                    my_pos = 0;
                } else {
                    my_pos = my_pos + 1;
                }
            }
        } while (bError);
        for (int j = 0; j < current_transaction_block.size(); j++) {
            final MerkleTreeOptimizedImp outer_tree = new MerkleTreeOptimizedImp();
            final ArrayList<MerkleNode> merkleNodeArrayList = new ArrayList<>();
            current_transaction_block.get(j).getTransactionList().forEach(val -> merkleNodeArrayList.add(new MerkleNode(val.getHash())));
            outer_tree.constructTree(merkleNodeArrayList);
            for (Map.Entry<Receipt.ReceiptBlock, List<Receipt>> entry : zone.entrySet()) {
                ArrayList<Receipt> receipt_list = (ArrayList<Receipt>) entry.getValue();
                publisher.withReceiptCountDownLatchSize(receipt_list.size());
                for (int i = 0; i < receipt_list.size(); i++) {
                    Receipt receipt = receipt_list.get(i);
                    int index = receipt.getPosition();
                    if (index < 0) {
                        LOG.info("Cannot find transaction in Transaction Block");
                        transactionBlock.setStatustype(StatusType.ABORT);
                        return;
                    }
                    Transaction transaction = current_transaction_block.get(j).getTransactionList().get(index);
                    boolean check = PreConditionsChecks(receipt, entry.getKey(), outer_tree, current_transaction_block.get(j), transaction, index);
                    boolean cross_check = CrossZoneConditionsChecks(current_transaction_block.get(j), entry.getKey());
                    if (!check || !cross_check)
                        atomicInteger.decrementAndGet();
                }
                publisher.getLatch().await();
                publisher.getJobSyncUntilRemainingCapacityZero();
            }
            outer_tree.clear();
        }
    }

    @SneakyThrows
    public boolean PreConditionsChecks(final Receipt receipt, final Receipt.ReceiptBlock receiptBlock, final MerkleTreeOptimizedImp outer_tree, final TransactionBlock transactionBlock, Transaction transaction, int index) {
        boolean bool3 = StringUtils.equals(transactionBlock.getMerkleRoot(), outer_tree.generateRoot(receipt.getProofs()));
        boolean bool5 = StringUtils.equals(receiptBlock.getOutboundMerkleRoot(), outer_tree.generateRoot(receipt.getProofs()));
        int val3 = Integer.compare(index, receipt.getPosition());
        int val4 = Integer.compare(transactionBlock.getHeight(), receiptBlock.getHeight());
        int val5 = Integer.compare(transactionBlock.getGeneration(), receiptBlock.getGeneration());
        int val6 = Integer.compare(receipt.getZoneTo(), CachedZoneIndex.getInstance().getZoneIndex());

        ReceiptBlock rcpBlock = new ReceiptBlock(StatusType.PENDING, receipt, transactionBlock, transaction);
        publisher.publish(rcpBlock);

        if (rcpBlock.getStatusType().equals(StatusType.ABORT)) {
            LOG.info("Receipt has errors aborted");
            return false;
        }

        if (val3 == 0 && val4 == 0.0 && val5 == 0 && val6 == 0 && bool3 && bool5)
            return true;
        return false;
    }

    public boolean CrossZoneConditionsChecks(final TransactionBlock transactionBlock, final Receipt.ReceiptBlock receiptBlock) {
        return StringUtils.equals(transactionBlock.getMerkleRoot(), receiptBlock.getOutboundMerkleRoot()) ? true : false;
    }
}
