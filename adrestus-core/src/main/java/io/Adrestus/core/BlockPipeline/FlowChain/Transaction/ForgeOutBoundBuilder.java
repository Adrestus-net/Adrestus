package io.Adrestus.core.BlockPipeline.FlowChain.Transaction;

import io.Adrestus.Trie.MerkleNode;
import io.Adrestus.Trie.MerkleTreeOptimizedImp;
import io.Adrestus.core.BlockPipeline.BlockRequest;
import io.Adrestus.core.BlockPipeline.BlockRequestHandler;
import io.Adrestus.core.BlockPipeline.BlockRequestType;
import io.Adrestus.core.OutBoundRelay;
import io.Adrestus.core.Receipt;
import io.Adrestus.core.Transaction;
import io.Adrestus.core.TransactionBlock;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ForgeOutBoundBuilder implements BlockRequestHandler<TransactionBlock> {
    private MerkleTreeOptimizedImp tree;
    private ArrayList<MerkleNode> merkleNodeArrayList;
    private ArrayList<Receipt> receiptList;

    public ForgeOutBoundBuilder() {
    }

    private void Init() {
        tree = new MerkleTreeOptimizedImp();
        merkleNodeArrayList = new ArrayList<>();
        receiptList = new ArrayList<>();
    }

    @Override
    public boolean canHandleRequest(BlockRequest<TransactionBlock> req) {
        return req.getRequestType() == BlockRequestType.FORGE_OUTBOUND_BUILDER;
    }

    @Override
    public int getPriority() {
        return 3;
    }

    @Override
    @SneakyThrows
    public void process(BlockRequest<TransactionBlock> blockRequest) {
        this.Init();
        blockRequest.getBlock().getTransactionList().forEach(transaction -> merkleNodeArrayList.add(new MerkleNode(transaction.getHash())));
        tree.constructTree(merkleNodeArrayList);
        blockRequest.getBlock().setMerkleRoot(tree.getRootHash());

        //##########OutBound############
        Receipt.ReceiptBlock receiptBlock = new Receipt.ReceiptBlock(blockRequest.getBlock().getHeight(), blockRequest.getBlock().getGeneration(), blockRequest.getBlock().getMerkleRoot());
        for (int i = 0; i < blockRequest.getBlock().getTransactionList().size(); i++) {
            Transaction transaction = blockRequest.getBlock().getTransactionList().get(i);
            if (transaction.getZoneFrom() != transaction.getZoneTo()) {
                MerkleNode node = new MerkleNode(transaction.getHash());
                tree.build_proofs(node);
                receiptList.add(new Receipt(transaction.getZoneFrom(), transaction.getZoneTo(), receiptBlock, tree.getMerkleeproofs(), i));
            }
        }

        Map<Integer, Map<Receipt.ReceiptBlock, List<Receipt>>> outbound = receiptList
                .stream()
                .collect(Collectors.groupingBy(Receipt::getZoneTo, Collectors.groupingBy(Receipt::getReceiptBlock)));

        OutBoundRelay outBoundRelay = new OutBoundRelay(outbound);
        blockRequest.getBlock().setOutbound(outBoundRelay);
    }

    @Override
    public void clear(BlockRequest<TransactionBlock> blockRequest) {
        blockRequest.clear();
        tree.clear();
        merkleNodeArrayList.forEach(MerkleNode::clear);
        merkleNodeArrayList.clear();
        receiptList.clear();
        tree = null;
        merkleNodeArrayList = null;
        receiptList = null;
    }

    @Override
    public String name() {
        return "ForgeOutBoundBuilder";
    }
}
