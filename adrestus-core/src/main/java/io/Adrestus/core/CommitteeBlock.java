package io.Adrestus.core;

import com.google.common.base.Objects;
import io.Adrestus.core.RingBuffer.handler.blocks.DisruptorBlock;
import io.Adrestus.core.RingBuffer.handler.blocks.DisruptorBlockVisitor;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.activej.serializer.annotations.Serialize;

import java.util.HashMap;
import java.util.Map;

public class CommitteeBlock extends AbstractBlock implements BlockFactory, DisruptorBlock {
    private String CommitteeProposer;
    private String VRF;
    private String VDF;
    private Map<BLSPublicKey, Double> StakingMap;
    private Map<Integer, HashMap<BLSPublicKey, String>> StructureMap;
    private int Difficulty;

    public CommitteeBlock(String previousHash, int height, int Generation, String committeeProposer, String VRF, String VDF, int Difficulty) {
        super(previousHash, height, Generation);
        CommitteeProposer = committeeProposer;
        this.VRF = VRF;
        this.VDF = VDF;
        this.StakingMap = new HashMap<BLSPublicKey, Double>();
        this.StructureMap = new HashMap<Integer, HashMap<BLSPublicKey, String>>();
        this.Difficulty = Difficulty;
        Init();
    }


    public CommitteeBlock() {
        this.CommitteeProposer = "";
        this.VRF = "";
        this.VDF = "";
        this.StakingMap = new HashMap<BLSPublicKey, Double>();
        this.StructureMap = new HashMap<Integer, HashMap<BLSPublicKey, String>>();
        this.Difficulty = 0;
        Init();
    }

    private void Init() {
        StructureMap.put(1, new HashMap<BLSPublicKey, String>());
        StructureMap.put(2, new HashMap<BLSPublicKey, String>());
        StructureMap.put(3, new HashMap<BLSPublicKey, String>());
        StructureMap.put(4, new HashMap<BLSPublicKey, String>());
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
    public String getCommitteeProposer() {
        return CommitteeProposer;
    }

    public void setCommitteeProposer(String committeeProposer) {
        CommitteeProposer = committeeProposer;
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
    public Map<BLSPublicKey, Double> getStakingMap() {
        return StakingMap;
    }

    public void setStakingMap(Map<BLSPublicKey, Double> stakingMap) {
        StakingMap = stakingMap;
    }

    @Serialize
    public Map<Integer, HashMap<BLSPublicKey, String>> getStructureMap() {
        return StructureMap;
    }

    public void setStructureMap(Map<Integer, HashMap<BLSPublicKey, String>> structureMap) {
        StructureMap = structureMap;
    }

    @Serialize
    public int getDifficulty() {
        return Difficulty;
    }

    public void setDifficulty(int difficulty) {
        Difficulty = difficulty;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        CommitteeBlock that = (CommitteeBlock) o;
        return Difficulty == that.Difficulty && Objects.equal(CommitteeProposer, that.CommitteeProposer) && Objects.equal(VRF, that.VRF) && Objects.equal(VDF, that.VDF) && Objects.equal(StakingMap, that.StakingMap) && Objects.equal(StructureMap, that.StructureMap);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), CommitteeProposer, VRF, VDF, StakingMap, StructureMap, Difficulty);
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
                ", Difficulty=" + Difficulty +
                '}';
    }
}
