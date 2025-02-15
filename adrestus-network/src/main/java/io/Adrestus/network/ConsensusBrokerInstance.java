package io.Adrestus.network;

import java.util.ArrayList;

public final class ConsensusBrokerInstance {
    private static volatile ConsensusBrokerInstance instance;
    private static ConsensusBroker consensusBroker;

    private ConsensusBrokerInstance() {
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        }
    }

    public static ConsensusBrokerInstance getInstance(ArrayList<String> ips, String leader, int position) {
        var result = instance;
        if (result == null) {
            synchronized (ConsensusBrokerInstance.class) {
                result = instance;
                if (result == null) {
                    result = new ConsensusBrokerInstance();
                    instance = result;
                    consensusBroker = new ConsensusBroker(ips, leader, position);
                    consensusBroker.initializeKafkaKingdom();
                }
            }
        }
        return result;
    }

    public static ConsensusBrokerInstance getInstance() {
        var result = instance;
        if (result == null) {
            synchronized (ConsensusBrokerInstance.class) {
                result = instance;
                if (result == null) {
                    result = new ConsensusBrokerInstance();
                    instance = result;
                }
            }
        }
        return result;
    }

    public ConsensusBroker getConsensusBroker() {
        return consensusBroker;
    }

    public void close() {
        instance = null;
        consensusBroker.shutDownGracefully();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
