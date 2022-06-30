package io.Adrestus.core.Trie;

import io.Adrestus.crypto.HashUtil;

import java.util.ArrayList;
import java.util.List;

public class MerkleTreeImp implements MerkleTree {

    private MekleNode root;
    private MekleNode buildable = new MekleNode();
    // private ArrayList<MekleeNode> proofs = new ArrayList<MekleeNode>();

    private MerkleProofs merkleeproofs = new MerkleProofs(new ArrayList<MekleNode>());
    private int pos = 0;

    @Override
    public boolean isMekleeNodeExisted(List<MekleNode> list, String roothash, MekleNode node) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void my_generate(List<MekleNode> dataBlocks) {

        for (int i = 0; i < dataBlocks.size(); i++) {
            dataBlocks.get(i).setTransactionHash(HashUtil.sha256(dataBlocks.get(i).getTransactionHash()));
        }

        buildTree2(dataBlocks);
    }

    public void buildTree2(List<MekleNode> list) {
        if (list.size() == 1) {
            this.root = list.get(0);
            return;
        }

        //list.stream().forEach(x->System.out.println(x.getTransactionHash()));
        ArrayList<MekleNode> iterate = new ArrayList<MekleNode>();
        int i = 0;
        while (list.size() > i) {
            MekleNode node = new MekleNode();
            node.setLeft(list.get(i));

            if (list.size() - 1 >= i + 1) {
                node.setRight(list.get(i + 1));
                node.setTransactionHash(HashUtil.sha256(node.getLeft().getTransactionHash() + node.getRight().getTransactionHash()));
            } else {
                node.setTransactionHash(node.getLeft().getTransactionHash());
                // System.out.println("edw"+node.getTransactionHash());
            }
            iterate.add(node);
            i = i + 2;
        }

        if (iterate.size() == 1) {
            //System.out.println(iterate.get(0).getLeft().getTransactionHash());
            // System.out.println(iterate.get(0).getRight().getTransactionHash());
            this.root = iterate.get(0);
            return;
        }
        //iterate.stream().forEach(x->System.out.println(x.getTransactionHash()));
        buildTree2(iterate);
    }

    public String getRootHash() {

        return this.root.getTransactionHash();
    }

    @Override
    public void build_proofs(List<MekleNode> dataBlocks, MekleNode current) {

        for (int i = 0; i < dataBlocks.size(); i++) {
            dataBlocks.get(i).setTransactionHash(HashUtil.sha256(dataBlocks.get(i).getTransactionHash()));
        }

        this.merkleeproofs.setTarget(current);
        proofs(dataBlocks, current);
    }

    private void proofs(List<MekleNode> list, MekleNode current) {
        int position = list.indexOf(current);
        ArrayList<MekleNode> iterate = new ArrayList<MekleNode>();
        int i = 0;
        while (list.size() > i) {
            MekleNode node = new MekleNode();
            node.setLeft(list.get(i));

            if (list.size() - 1 >= i + 1) {
                node.setRight(list.get(i + 1));
                node.setTransactionHash(HashUtil.sha256(node.getLeft().getTransactionHash() + node.getRight().getTransactionHash()));
                if (position == i) {
                    merkleeproofs.getList_builder().add(new MekleNode(null, list.get(i + 1)));
                    current = node;
                }
            } else {
                node.setTransactionHash(node.getLeft().getTransactionHash());
                if (position == i) {
                    MekleNode left = new MekleNode(node, null);
                    merkleeproofs.getList_builder().add(left);
                    current = node;

                    if (!merkleeproofs.getList_builder().get(merkleeproofs.getList_builder().size() - 1).getLeft().getTransactionHash().equals(left.getTransactionHash())) {
                        merkleeproofs.getList_builder().remove(left);
                    }

                }
            }
            if (position == i + 1) {
                merkleeproofs.getList_builder().add(new MekleNode(list.get(i), null));
                current = node;
            }

            iterate.add(node);

            i = i + 2;
        }
        if (iterate.size() == 1) {
            return;
        }
        proofs(iterate, current);
    }

    @Override
    public MerkleProofs getMerkleeproofs() {
        return merkleeproofs;
    }
}
