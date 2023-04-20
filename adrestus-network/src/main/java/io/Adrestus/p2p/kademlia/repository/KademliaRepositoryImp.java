package io.Adrestus.p2p.kademlia.repository;

import io.Adrestus.TreeFactory;
import io.Adrestus.config.StakingConfiguration;
import io.Adrestus.crypto.HashUtil;
import io.Adrestus.crypto.elliptic.ECDSASign;
import org.apache.commons.codec.binary.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class KademliaRepositoryImp implements KademliaRepository<String, KademliaData> {
    private static Logger LOG = LoggerFactory.getLogger(KademliaRepositoryImp.class);
    private final Map<String, KademliaData> stored_map;
    private final ECDSASign ecdsaSign;

    public KademliaRepositoryImp() {
        this.stored_map = new HashMap<>();
        this.ecdsaSign = new ECDSASign();
    }

    @Override
    public void store(String key, KademliaData value) {
        boolean verify = ecdsaSign.secp256Verify(HashUtil.sha256(StringUtils.getBytesUtf8(value.getAddressData().getAddress())), value.getAddressData().getAddress(), value.getAddressData().getECDSASignature());
        if (!verify) {
            LOG.info("Kademlia Data are not valid abort");
            return;
        }
        try {
            if (TreeFactory.getMemoryTree(0).getByaddress(value.getAddressData().getAddress()).get().getAmount() < StakingConfiguration.MINIMUM_STAKING) {
                LOG.info("Amount of this address not meet minimum requirements");
                return;
            }

        } catch (Exception e) {
            LOG.info("This address not found invalid");
            return;
        }
        stored_map.put(key, value);
    }

    @Override
    public KademliaData get(String key) {
        return stored_map.get(key);
    }

    @Override
    public void remove(String key) {
        stored_map.remove(key);
    }

    @Override
    public boolean contains(String key) {
        if (stored_map.containsKey(key))
            return true;
        return false;
    }

    @Override
    public List<String> getList() {
        return stored_map.keySet().stream().collect(Collectors.toList());
    }
}
