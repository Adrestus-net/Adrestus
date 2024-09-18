package io.Adrestus.core.RewardMechanism;

import io.Adrestus.TreeFactory;
import io.Adrestus.Trie.PatriciaTreeNode;
import io.Adrestus.config.StakingConfiguration;
import io.Adrestus.util.bytes.Bytes53;

import java.util.List;
import java.util.stream.Collectors;

public class EffectiveStakeCalculator implements RewardHandler {
    @Override
    public boolean canHandleRequest(Request req) {
        return req.getRequestType() == RequestType.EFFECTIVE_STAKE;
    }

    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    public void handle(Request req) {
        req.markHandled();
        List<String> adresses = TreeFactory.getMemoryTree(0).Keyset(Bytes53.ZERO, Integer.MAX_VALUE).stream().collect(Collectors.toList());
        int stake_counter=0;
        double total_stake=0;
        for(String address : adresses) {
            PatriciaTreeNode patriciaTreeNode=TreeFactory.getMemoryTree(0).getByaddress(address).get();
            if(patriciaTreeNode.getStaking_amount()>0){
                stake_counter++;
                total_stake += patriciaTreeNode.getStaking_amount();
            }
        }

        for(String address : adresses) {
            PatriciaTreeNode patriciaTreeNode=TreeFactory.getMemoryTree(0).getByaddress(address).get();
            if(patriciaTreeNode.getStaking_amount()>0){
               double effective_stake=Math.max(Math.min((1+StakingConfiguration.C)*total_stake/stake_counter,patriciaTreeNode.getStaking_amount()),(1-StakingConfiguration.C)*total_stake/stake_counter);
                CachedRewardMapData.getInstance().getEffective_stakes_map().put(address,new RewardObject(effective_stake,0));
            }
        }
    }

    @Override
    public String name() {
        return "EffectiveStakeCommand";
    }
}
