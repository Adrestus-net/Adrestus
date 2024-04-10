package io.Adrestus.core.RingBuffer.handler.blocks;

import io.Adrestus.MemoryTreePool;
import io.Adrestus.TreeFactory;
import io.Adrestus.core.Resourses.CachedInboundTransactionBlocks;
import io.Adrestus.core.Resourses.CachedZoneIndex;
import io.Adrestus.core.RingBuffer.event.AbstractBlockEvent;
import io.Adrestus.core.StatusType;
import io.Adrestus.core.Transaction;
import io.Adrestus.core.TransactionBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PatriciaTreeEventHandler implements BlockEventHandler<AbstractBlockEvent> {
    private static Logger LOG = LoggerFactory.getLogger(PatriciaTreeEventHandler.class);

    @Override
    public void onEvent(AbstractBlockEvent blockEvent, long l, boolean b) throws Exception {
        TransactionBlock transactionBlock = (TransactionBlock) blockEvent.getBlock();
        MemoryTreePool replica = (MemoryTreePool) ((MemoryTreePool) TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex())).clone();

        if (!transactionBlock.getTransactionList().isEmpty()) {
            for (int i = 0; i < transactionBlock.getTransactionList().size(); i++) {
                Transaction transaction = transactionBlock.getTransactionList().get(i);
                if ((transaction.getZoneFrom() == CachedZoneIndex.getInstance().getZoneIndex()) && (transaction.getZoneTo() == CachedZoneIndex.getInstance().getZoneIndex())) {
                    replica.withdrawReplica(transaction.getFrom(), transaction.getAmount(), replica);
                    replica.depositReplica(transaction.getTo(), transaction.getAmount(), replica);
                } else {
                    replica.withdrawReplica(transaction.getFrom(), transaction.getAmount(), replica);
                }
            }
        }

        CachedInboundTransactionBlocks.getInstance().generate(transactionBlock.getInbound().getMap_receipts());
        if (!transactionBlock.getInbound().getMap_receipts().isEmpty())
            transactionBlock
                    .getInbound()
                    .getMap_receipts()
                    .get(transactionBlock.getInbound().getMap_receipts().keySet().toArray()[0])
                    .entrySet()
                    .stream()
                    .forEach(entry -> {
                        entry.getValue().stream().forEach(receipt -> {
                            TransactionBlock block = CachedInboundTransactionBlocks.getInstance().retrieve(receipt.getZoneFrom(), receipt.getReceiptBlock().getHeight());
                            Transaction trx = block.getTransactionList().get(receipt.getPosition());
                            replica.depositReplica(trx.getFrom(), trx.getAmount(), replica);
                        });

                    });

        if (!replica.getRootHash().equals(transactionBlock.getPatriciaMerkleRoot())) {
//            Type fluentType = new TypeToken<MemoryTreePool>() {
//            }.getType();
//            List<SerializationUtil.Mapping> list = new ArrayList<>();
//            list.add(new SerializationUtil.Mapping(MemoryTreePool.class, ctx -> new MemoryTreePoolSerializer()));
//            List<SerializationUtil.Mapping> list2 = new ArrayList<>();
//            SerializationUtil patricia_tree_wrapper = new SerializationUtil<>(fluentType, list);
//            IBlockIndex blockIndex = new BlockIndex();
//            IDatabase<String, CommitteeBlock> committeeBlockIDatabase = new DatabaseFactory(String.class, CommitteeBlock.class).getDatabase(DatabaseType.ROCKS_DB, DatabaseInstance.COMMITTEE_BLOCK);
//            CommitteeBlock last= CachedLatestBlocks.getInstance().getCommitteeBlock();
//            Map<String, CommitteeBlock> lists=committeeBlockIDatabase.findBetweenRange(String.valueOf(CachedLatestBlocks.getInstance().getCommitteeBlock().getHeight()-1));
//            MemoryTreePool a= (MemoryTreePool) TreeFactory.getMemoryTree(CachedZoneIndex.getInstance().getZoneIndex());
//            List<String> new_ips = CachedLatestBlocks.getInstance().getCommitteeBlock().getStructureMap().get(CachedZoneIndex.getInstance().getZoneIndex()).values().stream().collect(Collectors.toList());
//            new_ips.remove(IPFinder.getLocalIP());
//            int RPCPatriciaTreeZonePort = ZoneDatabaseFactory.getDatabasePatriciaRPCPort(ZoneDatabaseFactory.getPatriciaTreeZoneInstance(CachedZoneIndex.getInstance().getZoneIndex()));
//            ArrayList<InetSocketAddress> toConnectPatricia = new ArrayList<>();
//            new_ips.stream().forEach(ip -> {
//                try {
//                    toConnectPatricia.add(new InetSocketAddress(InetAddress.getByName(ip), RPCPatriciaTreeZonePort));
//                } catch (UnknownHostException e) {
//                    throw new RuntimeException(e);
//                }
//            });
//            RpcAdrestusClient client = new RpcAdrestusClient(new byte[]{}, toConnectPatricia, CachedEventLoop.getInstance().getEventloop());
//            client.connect();
//            List<byte[]>  treeObjects = client.getPatriciaTreeList("");
//            MemoryTreePool ar= (MemoryTreePool) patricia_tree_wrapper.decode(treeObjects.get(treeObjects.size()));
//            MemoryTreePool ar2= (MemoryTreePool) patricia_tree_wrapper.decode(treeObjects.get(treeObjects.size()-1));
//            MemoryTreePool ar4= (MemoryTreePool) patricia_tree_wrapper.decode(treeObjects.get(treeObjects.size()-2));
            LOG.info("Patricia Merkle root is invalid abort");
            transactionBlock.setStatustype(StatusType.ABORT);
            return;
        }
    }
}
