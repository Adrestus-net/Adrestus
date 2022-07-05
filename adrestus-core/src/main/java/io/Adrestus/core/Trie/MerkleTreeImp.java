package io.Adrestus.core.Trie;

import io.Adrestus.crypto.HashUtil;

import java.util.ArrayList;
import java.util.List;

public class MerkleTreeImp implements MerkleTree {

    private MerkleNode root;
    // private ArrayList<MekleeNode> proofs = new ArrayList<MekleeNode>();

    private MerkleProofs MerkleProofs;// = new MerkleProofs(new ArrayList<MerkleNode>());
    private int pos = 0;

    @Override
    public boolean isMekleeNodeExisted(List<MerkleNode> list, String roothash, MerkleNode node) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void my_generate(List<MerkleNode> dataBlocks) {
        ArrayList<MerkleNode> current = new ArrayList<MerkleNode>();
        for (int i = 0; i < dataBlocks.size(); i++) {
            MerkleNode node=new MerkleNode(HashUtil.sha256(dataBlocks.get(i).getTransactionHash()));
            current.add(node);
        }

        buildTree(current);
    }

    public void my_generate2(List<MerkleNode> dataBlocks) {
        ArrayList<MerkleNode> current = new ArrayList<MerkleNode>();
        for (int i = 0; i < dataBlocks.size(); i++) {
            MerkleNode node=new MerkleNode(HashUtil.sha256(dataBlocks.get(i).getTransactionHash()));
            current.add(node);
        }

        buildTree2(current).result();
    }
    public void buildTree(ArrayList<MerkleNode> list) {
        if (list.size() == 1) {
            this.root = list.get(0);
            return;
        }

        //list.stream().forEach(x->System.out.println(x.getTransactionHash()));
        ArrayList<MerkleNode> iterate = new ArrayList<MerkleNode>();
        int i = 0;
        while (list.size() > i) {
            MerkleNode node = new MerkleNode();
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
        buildTree(iterate);
    }

    public RecursiveOptimizer<Integer> buildTree2(ArrayList<MerkleNode> list) {
        if (list.size() == 1) {
            this.root = list.get(0);
            return RecursiveOptimizer.done(0);
        }

        //list.stream().forEach(x->System.out.println(x.getTransactionHash()));
        ArrayList<MerkleNode> iterate = new ArrayList<MerkleNode>();
        int i = 0;
        while (list.size() > i) {
            MerkleNode node = new MerkleNode();
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
            return RecursiveOptimizer.done(0);
        }
        //iterate.stream().forEach(x->System.out.println(x.getTransactionHash()));
        return RecursiveOptimizer.more(() -> buildTree2(iterate));
    }

    public String getRootHash() {

        return this.root.getTransactionHash();
    }


    @Override
    public io.Adrestus.core.Trie.MerkleProofs getMerkleeproofs() {
        return MerkleProofs;
    }

    @Override
    public void build_proofs(List<MerkleNode> dataBlocks, MerkleNode current) {
        ArrayList<MerkleNode> current_list = new ArrayList<MerkleNode>();
        for (int i = 0; i < dataBlocks.size(); i++) {
            MerkleNode node=new MerkleNode(HashUtil.sha256(dataBlocks.get(i).getTransactionHash()));
            current_list.add(node);
        }

        this.MerkleProofs = new MerkleProofs(new ArrayList<MerkleNode>());
        current.setTransactionHash(HashUtil.sha256(current.getTransactionHash()));
        this.MerkleProofs.setTarget(current);
        proofs(current_list, current);
    }

    public void build_proofs2(List<MerkleNode> dataBlocks, MerkleNode current) {
        ArrayList<MerkleNode> current_list = new ArrayList<MerkleNode>();
        for (int i = 0; i < dataBlocks.size(); i++) {
            MerkleNode node=new MerkleNode(HashUtil.sha256(dataBlocks.get(i).getTransactionHash()));
            current_list.add(node);
        }

        this.MerkleProofs = new MerkleProofs(new ArrayList<MerkleNode>());
        current.setTransactionHash(HashUtil.sha256(current.getTransactionHash()));
        this.MerkleProofs.setTarget(current);
        proofs2(current_list, current).result();
    }

    private void proofs(List<MerkleNode> list, MerkleNode current) {
        int position = -1;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getTransactionHash().equals(current.getTransactionHash())) {
                position = i;
            }
        }
        if(position==-1)
            System.out.println("problima");
        ArrayList<MerkleNode> iterate = new ArrayList<MerkleNode>();
        int i = 0;
        while (list.size() > i) {
            MerkleNode node = new MerkleNode();
            node.setLeft(list.get(i));

            if (list.size() - 1 >= i + 1) {
                node.setRight(list.get(i + 1));
                node.setTransactionHash(HashUtil.sha256(node.getLeft().getTransactionHash() + node.getRight().getTransactionHash()));
                if (position == i) {
                    MerkleProofs.getList_builder().add(new MerkleNode(null, list.get(i + 1)));
                    current = node;
                }
            } else {
                node.setTransactionHash(node.getLeft().getTransactionHash());
                if (position == i) {
                    MerkleNode left = new MerkleNode(node, null);
                    MerkleProofs.getList_builder().add(left);
                    current = node;

                    if (!MerkleProofs.getList_builder().get(MerkleProofs.getList_builder().size() - 1).getLeft().getTransactionHash().equals(left.getTransactionHash())) {
                        MerkleProofs.getList_builder().remove(left);
                    }

                }
            }
            if (position == i + 1) {
                MerkleProofs.getList_builder().add(new MerkleNode(list.get(i), null));
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

    private RecursiveOptimizer<Integer> proofs2(List<MerkleNode> list, final MerkleNode current) {
        int position = -1;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getTransactionHash().equals(current.getTransactionHash())) {
                position = i;
            }
        }
        if(position==-1)
            System.out.println("problima");
        ArrayList<MerkleNode> iterate = new ArrayList<MerkleNode>();
        MerkleNode node = new MerkleNode();
        int i = 0;
        while (list.size() > i) {

            node.setLeft(list.get(i));

            if (list.size() - 1 >= i + 1) {
                node.setRight(list.get(i + 1));
                node.setTransactionHash(HashUtil.sha256(node.getLeft().getTransactionHash() + node.getRight().getTransactionHash()));
                if (position == i) {
                    MerkleProofs.getList_builder().add(new MerkleNode(null, list.get(i + 1)));
                }
            } else {
                node.setTransactionHash(node.getLeft().getTransactionHash());
                if (position == i) {
                    MerkleNode left = new MerkleNode(node, null);
                    MerkleProofs.getList_builder().add(left);

                    if (!MerkleProofs.getList_builder().get(MerkleProofs.getList_builder().size() - 1).getLeft().getTransactionHash().equals(left.getTransactionHash())) {
                        MerkleProofs.getList_builder().remove(left);
                    }

                }
            }
            if (position == i + 1) {
                MerkleProofs.getList_builder().add(new MerkleNode(list.get(i), null));
            }

            iterate.add(node);

            i = i + 2;
        }
        if (iterate.size() == 1) {
            return RecursiveOptimizer.done(0);
        }
        return RecursiveOptimizer.more(() -> proofs2(iterate, node));
    }
    public String GenerateRoot(MerkleProofs proofs) {
        String hash = proofs.getTarget().getTransactionHash();
        for (int i = 0; i < proofs.getList_builder().size(); i++) {
            if (proofs.getList_builder().get(i).getLeft() != null) {
                hash = HashUtil.sha256((proofs.getList_builder().get(i).getLeft().getTransactionHash() + hash));
            }
            if (proofs.getList_builder().get(i).getRight() != null) {
                hash = HashUtil.sha256((hash + proofs.getList_builder().get(i).getRight().getTransactionHash()));
            }
        }
        return hash;
    }
}
