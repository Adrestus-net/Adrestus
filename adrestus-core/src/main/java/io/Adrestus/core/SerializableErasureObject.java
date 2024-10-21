package io.Adrestus.core;

import io.Adrestus.Trie.MerkleProofs;
import io.Adrestus.Trie.MerkleTreeOptimizedImp;
import io.Adrestus.erasure.code.parameters.FECParameterObject;
import io.activej.serializer.annotations.Serialize;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class SerializableErasureObject {

    private String rootMerkleHash;
    private FECParameterObject fecParameterObject;
    private byte[] originalPacketChunks;

    private ArrayList<byte[]> repairPacketChunks;

    private MerkleProofs proofs;


    public SerializableErasureObject() {
    }

    public SerializableErasureObject(FECParameterObject fecParameterObject, byte[] originalPacketChunks) {
        this.fecParameterObject = fecParameterObject;
        this.originalPacketChunks = originalPacketChunks;
        this.repairPacketChunks = new ArrayList<>();
        this.rootMerkleHash = "";
        this.proofs = new MerkleProofs();
    }

    public SerializableErasureObject(FECParameterObject fecParameterObject, byte[] originalPacketChunks, ArrayList<byte[]> repairPacketChunks) {
        this.fecParameterObject = fecParameterObject;
        this.originalPacketChunks = originalPacketChunks;
        this.repairPacketChunks = repairPacketChunks;
        this.rootMerkleHash = "";
        this.proofs = new MerkleProofs();
    }

    public SerializableErasureObject(FECParameterObject fecParameterObject, byte[] originalPacketChunks, MerkleProofs proofs) {
        this.fecParameterObject = fecParameterObject;
        this.originalPacketChunks = originalPacketChunks;
        this.repairPacketChunks = new ArrayList<>();
        this.rootMerkleHash = "";
        this.proofs = proofs;
    }

    public SerializableErasureObject(FECParameterObject fecParameterObject, byte[] originalPacketChunks, ArrayList<byte[]> repairPacketChunks, MerkleProofs proofs) {
        this.fecParameterObject = fecParameterObject;
        this.originalPacketChunks = originalPacketChunks;
        this.repairPacketChunks = repairPacketChunks;
        this.rootMerkleHash = "";
        this.proofs = proofs;
    }

    @Serialize
    public FECParameterObject getFecParameterObject() {
        return fecParameterObject;
    }

    public void setFecParameterObject(FECParameterObject fecParameterObject) {
        this.fecParameterObject = fecParameterObject;
    }

    @Serialize
    public byte[] getOriginalPacketChunks() {
        return originalPacketChunks;
    }

    public void setOriginalPacketChunks(byte[] originalPacketChunks) {
        this.originalPacketChunks = originalPacketChunks;
    }

    @Serialize
    public ArrayList<byte[]> getRepairPacketChunks() {
        return repairPacketChunks;
    }

    public void setRepairPacketChunks(ArrayList<byte[]> repairPacketChunks) {
        this.repairPacketChunks = repairPacketChunks;
    }

    @Serialize
    public String getRootMerkleHash() {
        return rootMerkleHash;
    }

    public void setRootMerkleHash(String rootMerkleHash) {
        this.rootMerkleHash = rootMerkleHash;
    }


    @Serialize
    public MerkleProofs getProofs() {
        return proofs;
    }

    public void setProofs(MerkleProofs proofs) {
        this.proofs = proofs;
    }

    public int getSize() {
        double SumOfrepairPacketChunk = repairPacketChunks.stream().mapToDouble(this::sum).sum();
        double SumOMerkleNodes = getProofs().getLength();
        return (int) (this.fecParameterObject.getSize() + originalPacketChunks.length + SumOfrepairPacketChunk + SumOMerkleNodes) + 1024;
    }

    public int getRepairChunksSize() {
        return (int) repairPacketChunks.stream().mapToDouble(this::sum).sum();
    }

    private double sum(byte[] array) {
        return array.length;
    }

    public boolean CheckChunksValidity(String OriginalHash) {
        MerkleTreeOptimizedImp tree = new MerkleTreeOptimizedImp();
        String hash1 = tree.generateRoot(this.proofs);
        tree.clear();
        return OriginalHash.equals(hash1) && OriginalHash.equals(rootMerkleHash);
    }

    public boolean CheckChunksWithRepairValidity(String OriginalHash) {
        MerkleTreeOptimizedImp tree = new MerkleTreeOptimizedImp();
        String hash1 = tree.generateRoot(this.proofs);
        tree.clear();
        return OriginalHash.equals(hash1);

//        MerkleTreeImp tree = new MerkleTreeImp();
//        tree.build_proofs(merkleNodeArrayList, new MerkleNode(Hex.toHexString(HashUtil.Shake256(getOriginalPacketChunks()))));
//        MerkleProofs original_proofs = tree.getMerkleeproofs();
//
//        ByteBuffer allocate = ByteBuffer.allocate(getRepairChunksSize());
//        getRepairPacketChunks().forEach(buff -> allocate.put(buff));
//        tree.build_proofs(merkleNodeArrayList, new MerkleNode(Hex.toHexString(HashUtil.Shake256(allocate.array()))));
//        MerkleProofs repair_proofs = tree.getMerkleeproofs();
//
//        String hash1 = tree.GenerateRoot(original_proofs);
//        String hash2 = tree.GenerateRoot(repair_proofs);
//
//        return OriginalHash.equals(hash1) && OriginalHash.equals(hash2);
    }

    //DO NOT DELETE THAT FUNCTION ITS CUSTOM CHECK repairPacketChunks ARRAYLIST
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SerializableErasureObject that = (SerializableErasureObject) o;
        return Objects.equals(fecParameterObject, that.fecParameterObject) && Arrays.equals(originalPacketChunks, that.originalPacketChunks) && SerializableErasureObject.linkedEquals(repairPacketChunks, that.repairPacketChunks);
    }

    private static boolean linkedEquals(ArrayList<byte[]> first, ArrayList<byte[]> second) {
        for (int i = 0; i < first.size(); i++) {
            if (!Arrays.equals(first.get(i), second.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(fecParameterObject, repairPacketChunks);
        result = 31 * result + Arrays.hashCode(originalPacketChunks);
        return result;
    }
}
