package io.Adrestus.core;

import io.Adrestus.crypto.bls.model.BLSPublicKey;

import java.util.HashMap;
import java.util.Map;

public class CommitteeBlock extends AbstractBlock implements BlockFactory {
    private final String CommitteeProposer;
    private final String VRF;
    private final String VDF;
    private final Map<BLSPublicKey, Double> StakingMap;
    private final Map<Integer, HashMap<BLSPublicKey, String>> StructureMap;

    public CommitteeBlock(String previousHash, int height, int Generation, String committeeProposer, String VRF, String VDF) {
        super(previousHash, height, Generation);
        CommitteeProposer = committeeProposer;
        this.VRF = VRF;
        this.VDF = VDF;
        this.StakingMap = new HashMap<BLSPublicKey, Double>();
        this.StructureMap = new HashMap<Integer, HashMap<BLSPublicKey, String>>();
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
}
