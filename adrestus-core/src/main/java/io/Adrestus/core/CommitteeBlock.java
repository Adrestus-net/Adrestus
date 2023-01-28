package io.Adrestus.core;

import com.google.common.base.Objects;
import io.Adrestus.core.RingBuffer.handler.blocks.DisruptorBlock;
import io.Adrestus.core.RingBuffer.handler.blocks.DisruptorBlockVisitor;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.elliptic.mapper.StakingData;
import io.Adrestus.p2p.kademlia.repository.KademliaData;
import io.activej.serializer.annotations.Serialize;

import java.io.Serializable;
import java.util.*;

public class CommitteeBlock extends AbstractBlock implements BlockFactory, DisruptorBlock, Serializable {
    private int[] CommitteeProposer;
    private String VRF;
    private String VDF;
    private TreeMap<StakingData, KademliaData> StakingMap;
    private Map<Integer, LinkedHashMap<BLSPublicKey, String>> StructureMap;
    private int difficulty;

    public CommitteeBlock(String previousHash, int height, int Generation, int[] committeeProposer, String VRF, String VDF, int difficulty) {
        super(previousHash, height, Generation);
        this.CommitteeProposer = committeeProposer;
        this.VRF = VRF;
        this.VDF = VDF;
        this.StakingMap = new TreeMap<StakingData, KademliaData>(new StakingValueComparator());
        this.StructureMap = new HashMap<Integer, LinkedHashMap<BLSPublicKey, String>>();
        this.difficulty = difficulty;
        Init();
    }


    public CommitteeBlock() {
        this.CommitteeProposer = new int[0];
        this.VRF = "";
        this.VDF = "";
        this.StakingMap = new TreeMap<StakingData, KademliaData>(new StakingValueComparator());
        this.StructureMap = new HashMap<Integer, LinkedHashMap<BLSPublicKey, String>>();
        Init();
    }

    private void Init() {
        this.StructureMap.put(0, new LinkedHashMap<BLSPublicKey, String>());
        this.StructureMap.put(1, new LinkedHashMap<BLSPublicKey, String>());
        this.StructureMap.put(2, new LinkedHashMap<BLSPublicKey, String>());
        this.StructureMap.put(3, new LinkedHashMap<BLSPublicKey, String>());
    }

    public void createStructureMap() {
        this.StructureMap = new HashMap<Integer, LinkedHashMap<BLSPublicKey, String>>();
        this.StructureMap.put(0, new LinkedHashMap<BLSPublicKey, String>());
        this.StructureMap.put(1, new LinkedHashMap<BLSPublicKey, String>());
        this.StructureMap.put(2, new LinkedHashMap<BLSPublicKey, String>());
        this.StructureMap.put(3, new LinkedHashMap<BLSPublicKey, String>());
    }

    @Override
    public void accept(BlockForge visitor) {
        visitor.forgeCommitteBlock(this);
    }

    @Override
    public void accept(DisruptorBlockVisitor disruptorBlockVisitor) {
        disruptorBlockVisitor.visit(this);
    }

    @Serialize
    public int[] getCommitteeProposer() {
        return CommitteeProposer;
    }

    public void setCommitteeProposer(int[] committeeProposer) {
        this.CommitteeProposer = committeeProposer;
    }

    @Serialize
    public String getVRF() {
        return VRF;
    }

    public void setVRF(String VRF) {
        this.VRF = VRF;
    }

    @Serialize
    public String getVDF() {
        return VDF;
    }

    public void setVDF(String VDF) {
        this.VDF = VDF;
    }

    @Serialize
    public TreeMap<StakingData, KademliaData> getStakingMap() {
        return StakingMap;
    }

    public void setStakingMap(TreeMap<StakingData, KademliaData> stakingMap) {
        StakingMap = stakingMap;
    }

    @Serialize
    public Map<Integer, LinkedHashMap<BLSPublicKey, String>> getStructureMap() {
        return StructureMap;
    }


    public void setStructureMap(Map<Integer, LinkedHashMap<BLSPublicKey, String>> structureMap) {
        StructureMap = structureMap;
    }

    @Serialize
    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    @Override
    public int getViewID() {
        return ViewID;
    }

    @Override
    public void setViewID(int viewID) {
        ViewID = viewID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        CommitteeBlock that = (CommitteeBlock) o;
        return difficulty == that.difficulty && Arrays.equals(CommitteeProposer, that.CommitteeProposer) && Objects.equal(VRF, that.VRF) && Objects.equal(VDF, that.VDF) && Objects.equal(StakingMap, that.StakingMap) && Objects.equal(StructureMap, that.StructureMap);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), CommitteeProposer, VRF, VDF, StakingMap, StructureMap, difficulty);
    }

    @Override
    public String toString() {
        return super.toString() +
                "CommitteeBlock{" +
                "CommitteeProposer='" + CommitteeProposer + '\'' +
                ", VRF='" + VRF + '\'' +
                ", VDF='" + VDF + '\'' +
                ", StakingMap=" + StakingMap +
                ", StructureMap=" + StructureMap +
                ", Difficulty=" + difficulty +
                '}';
    }

    private static final class StakingValueComparator implements Comparator<StakingData>, Serializable {
        @Override
        public int compare(StakingData a, StakingData b) {
            if (a.getStake() >= b.getStake()) {
                return -1;
            } else {
                return 1;
            } // returning 0 would merge keys
        }
    }
}
