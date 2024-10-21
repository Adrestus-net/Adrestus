package io.Adrestus.core.RewardMechanism;

import io.Adrestus.TreeFactory;
import io.Adrestus.Trie.PatriciaTreeNode;
import io.Adrestus.config.RewardConfiguration;
import io.Adrestus.core.Resourses.CachedLatestBlocks;
import io.Adrestus.core.Resourses.CachedStartHeightRewards;
import io.Adrestus.core.TransactionBlock;
import io.Adrestus.crypto.bls.BLSSignatureData;
import io.Adrestus.crypto.bls.model.BLSPublicKey;
import io.Adrestus.crypto.bls.model.BLSSignature;
import io.Adrestus.crypto.elliptic.mapper.StakingData;
import io.Adrestus.p2p.kademlia.repository.KademliaData;
import io.Adrestus.util.CustomBigDecimal;
import io.distributedLedger.DatabaseFactory;
import io.distributedLedger.DatabaseType;
import io.distributedLedger.IDatabase;
import io.distributedLedger.ZoneDatabaseFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.TreeMap;

public class ValidatorRewardCalculator implements RewardHandler {
    @Override
    public boolean canHandleRequest(Request req) {
        return req.getRequestType() == RequestType.VALIDATOR_REWARD_CALCULATOR;
    }

    @Override
    public int getPriority() {
        return 4;
    }

    @Override
    public void handle(Request req) {
        req.markHandled();
        TreeMap<StakingData, KademliaData> stakingMap = CachedLatestBlocks.getInstance().getCommitteeBlock().getStakingMap();
        IDatabase<String, TransactionBlock> transactionBlockIDatabase = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, ZoneDatabaseFactory.getZoneInstance(0));
        Map<String, TransactionBlock> transactionBlockMap = transactionBlockIDatabase.findBetweenRange(String.valueOf(CachedStartHeightRewards.getInstance().getHeight()));
        stakingMap.values().stream().forEach(data -> {
            for (Map.Entry<String, TransactionBlock> entry : transactionBlockMap.entrySet()) {
                Map<BLSPublicKey, BLSSignatureData> bls_map = entry.getValue().getSignatureData();
                BLSSignatureData blsSignatureDataEntry = bls_map.get(data.getAddressData().getValidatorBlSPublicKey());
                if (blsSignatureDataEntry != null) {

                    boolean flag = false;
                    for (int i = 0; i < 2; i++) {
                        if (!BLSSignature.verify(blsSignatureDataEntry.getSignature()[i], blsSignatureDataEntry.getMessageHash()[i], data.getAddressData().getValidatorBlSPublicKey()))
                            flag = true;
                    }

                    if (!flag) {
                        CachedRewardMapData.getInstance().getEffective_stakes_map().get(data.getAddressData().getAddress()).setBlock_participation(CachedRewardMapData.getInstance().getEffective_stakes_map().get(data.getAddressData().getAddress()).getBlock_participation() + 1);
                        if (data.getAddressData().getValidatorBlSPublicKey().equals(entry.getValue().getLeaderPublicKey())) {
                            CachedRewardMapData.getInstance().getEffective_stakes_map().get(data.getAddressData().getAddress()).setTransactions_leader_participation(CachedRewardMapData.getInstance().getEffective_stakes_map().get(data.getAddressData().getAddress()).getTransactions_leader_participation() + 1);
                        }
                    }
                }
            }
            if (CachedLatestBlocks.getInstance().getCommitteeBlock().getLeaderPublicKey().equals(data.getAddressData().getValidatorBlSPublicKey()) && CachedStartHeightRewards.getInstance().isRewardsCommitteeEnabled())
                CachedRewardMapData.getInstance().getEffective_stakes_map().get(data.getAddressData().getAddress()).setCommittee_leader_participation(true);
        });


        for (Map.Entry<String, RewardObject> entry : CachedRewardMapData.getInstance().getEffective_stakes_map().entrySet()) {
            PatriciaTreeNode patriciaTreeNode = TreeFactory.getMemoryTree(0).getByaddress(entry.getKey()).get();
            BigDecimal block_reward = CustomBigDecimal.valueOf(entry.getValue().getBlock_participation()).multiply(CustomBigDecimal.valueOf(RewardConfiguration.TRANSACTION_REWARD_PER_BLOCK)).multiply(entry.getValue().getEffective_stake_ratio()).setScale(RewardConfiguration.DECIMAL_PRECISION, RewardConfiguration.ROUNDING);
            BigDecimal commission_fees = CustomBigDecimal.valueOf(patriciaTreeNode.getStakingInfo().getCommissionRate()).multiply(block_reward).divide(CustomBigDecimal.valueOf(100), RewardConfiguration.DECIMAL_PRECISION, RewardConfiguration.ROUNDING);
            BigDecimal unreal_reward = block_reward.subtract(commission_fees).setScale(RewardConfiguration.DECIMAL_PRECISION, RewardConfiguration.ROUNDING);
            BigDecimal real_reward = commission_fees.add(CustomBigDecimal.valueOf(patriciaTreeNode.getPrivate_staking_amount()).multiply(unreal_reward).divide(CustomBigDecimal.valueOf(patriciaTreeNode.getStaking_amount()), RewardConfiguration.DECIMAL_PRECISION, RewardConfiguration.ROUNDING));
            BigDecimal per_block = real_reward.divide(CustomBigDecimal.valueOf(entry.getValue().getBlock_participation()), RewardConfiguration.DECIMAL_PRECISION, RewardConfiguration.ROUNDING);

            //leader block rewards
            //Don't change rounding mode up problem here
            real_reward = real_reward.add(per_block.multiply(BigDecimal.valueOf(entry.getValue().getTransactions_leader_participation())).multiply(RewardConfiguration.TRANSACTION_LEADER_BLOCK_REWARD).setScale(RewardConfiguration.DECIMAL_PRECISION, RoundingMode.UP));
            if (entry.getValue().isCommittee_leader_participation()) {
                //committee leader VRF rewards
                real_reward = real_reward.add(per_block.multiply(RewardConfiguration.VDF_REWARD)).setScale(RewardConfiguration.DECIMAL_PRECISION, RewardConfiguration.ROUNDING);

                //committee leader VDF rewards
                real_reward = real_reward.add(per_block.multiply(RewardConfiguration.VRF_REWARD)).setScale(RewardConfiguration.DECIMAL_PRECISION, RewardConfiguration.ROUNDING);
            }

            entry.getValue().setUnreal_reward(unreal_reward);
            entry.getValue().setReal_reward(real_reward);
        }
    }

    @Override
    public String name() {
        return "VALIDATORREWARDCALCULATOR";
    }
}
