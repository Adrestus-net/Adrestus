package io.Adrestus.Trie;

import com.google.common.base.Objects;
import io.activej.serializer.annotations.Deserialize;
import io.activej.serializer.annotations.Serialize;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MerkleProofsCached implements Serializable {
    private String roothash;
    private List<MerkleNode> list_builder;
    private MerkleNode target;

    public MerkleProofsCached(String roothash, List<MerkleNode> list_builder, MerkleNode target) {
        this.roothash = roothash;
        this.list_builder = list_builder;
        this.target = target;
    }

    public MerkleProofsCached(@Deserialize("list_builder") List<MerkleNode> list_builder) {
        this.roothash = "";
        this.list_builder = list_builder;
        this.target = new MerkleNode("");
    }

    public MerkleProofsCached() {
        this.roothash = "";
        this.list_builder = new ArrayList<>();
        this.target = new MerkleNode("");
    }

    @Serialize
    public String getRoothash() {
        return roothash;
    }

    public void setRoothash(String roothash) {
        this.roothash = roothash;
    }

    @Serialize
    public List<MerkleNode> getList_builder() {
        return list_builder;
    }

    public void setList_builder(List<MerkleNode> list_builder) {
        this.list_builder = list_builder;
    }

    @Serialize
    public MerkleNode getTarget() {
        return target;
    }

    public void setTarget(MerkleNode target) {
        this.target = target;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MerkleProofsCached that = (MerkleProofsCached) o;
        return Objects.equal(roothash, that.roothash) && Objects.equal(list_builder, that.list_builder) && Objects.equal(target, that.target);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(roothash, list_builder, target);
    }

    @Override
    public String toString() {
        return "MerkleProofs{" +
                "roothash='" + roothash + '\'' +
                ", list_builder=" + list_builder +
                ", target=" + target +
                '}';
    }

    private void clearMerkleNode(MerkleNode node) {
        if (node == null) {
            return;
        }
        clearMerkleNode(node.getLeft());
        clearMerkleNode(node.getRight());
        node.setLeft(null);
        node.setRight(null);
        node.setTransactionHash(null);
    }
}
