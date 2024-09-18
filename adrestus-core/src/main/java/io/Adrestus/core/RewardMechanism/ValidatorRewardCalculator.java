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
import io.distributedLedger.DatabaseFactory;
import io.distributedLedger.DatabaseType;
import io.distributedLedger.IDatabase;
import io.distributedLedger.ZoneDatabaseFactory;

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
        stakingMap.values().stream().forEach(data -> {
            int block_participated = 0;
            int leader_participated = 0;
            IDatabase<String, TransactionBlock> transactionBlockIDatabase = new DatabaseFactory(String.class, TransactionBlock.class).getDatabase(DatabaseType.ROCKS_DB, ZoneDatabaseFactory.getZoneInstance(0));
            Map<String, TransactionBlock> transactionBlockMap = transactionBlockIDatabase.seekBetweenRange(CachedStartHeightRewards.getInstance().getHeight(), transactionBlockIDatabase.seekLast().get().getHeight());
            for (Map.Entry<String, TransactionBlock> entry : transactionBlockMap.entrySet()) {
                Map<BLSPublicKey, BLSSignatureData> bls_map = entry.getValue().getSignatureData();
                for (Map.Entry<BLSPublicKey, BLSSignatureData> bls_map_entry : bls_map.entrySet()) {
                    if (data.getAddressData().getValidatorBlSPublicKey().equals(bls_map_entry.getKey())) {

                        boolean flag = false;
                        for (int i = 0; i < 2; i++) {
                            if (!BLSSignature.verify(bls_map_entry.getValue().getSignature()[0], bls_map_entry.getValue().getMessageHash()[0], bls_map_entry.getKey()))
                                flag = true;
                        }
                        if (flag)
                            continue;
                        block_participated++;
                        if (data.getAddressData().getValidatorBlSPublicKey().equals(entry.getValue().getLeaderPublicKey()))
                            leader_participated++;
                    }
                }
            }
            if (CachedLatestBlocks.getInstance().getCommitteeBlock().getLeaderPublicKey().equals(data.getAddressData().getValidatorBlSPublicKey()) && !CachedStartHeightRewards.getInstance().isRewardsCommitteeEnabled())
                CachedRewardMapData.getInstance().getEffective_stakes_map().get(data.getAddressData().getAddress()).setCommittee_leader_participation(true);
            CachedRewardMapData.getInstance().getEffective_stakes_map().get(data.getAddressData().getAddress()).setBlock_participation(block_participated);
            CachedRewardMapData.getInstance().getEffective_stakes_map().get(data.getAddressData().getAddress()).setTransactions_leader_participation(leader_participated);
        });


        for (Map.Entry<String, RewardObject> entry : CachedRewardMapData.getInstance().getEffective_stakes_map().entrySet()) {
            PatriciaTreeNode patriciaTreeNode = TreeFactory.getMemoryTree(0).getByaddress(entry.getKey()).get();
            double block_reward = entry.getValue().getBlock_participation() * RewardConfiguration.TRANSACTION_REWARD_PER_BLOCK * entry.getValue().getEffective_stake_ratio();
            double commission_fees = patriciaTreeNode.getStakingInfo().getCommissionRate() / 100 * block_reward;
            double unreal_reward = block_reward - commission_fees;
            double real_reward = patriciaTreeNode.getPrivate_staking_amount() / patriciaTreeNode.getStaking_amount() * unreal_reward;

            //leader block rewards
            real_reward = real_reward + (((double) RewardConfiguration.TRANSACTION_LEADER_BLOCK_REWARD / 100 * real_reward) * entry.getValue().getTransactions_leader_participation());

            if (entry.getValue().isCommittee_leader_participation()) {
                //committee leader VRF rewards
                real_reward = real_reward + ((double) RewardConfiguration.VRF_REWARD / 100 * real_reward);

                //committee leader VDF rewards
                real_reward = real_reward + ((double) RewardConfiguration.VDF_REWARD / 100 * real_reward);
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
