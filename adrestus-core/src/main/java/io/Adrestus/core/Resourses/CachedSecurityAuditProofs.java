package io.Adrestus.core.Resourses;

import io.Adrestus.p2p.kademlia.repository.KademliaData;

import java.util.ArrayList;
import java.util.List;

public class CachedSecurityAuditProofs {

    private static volatile CachedSecurityAuditProofs instance;

    private static volatile List<KademliaData> securityAuditProofs;

    /**
     * private constructor to prevent client from instantiating.
     */
    private CachedSecurityAuditProofs() {
        // to prevent instantiating by Reflection call
        if (instance != null) {
            throw new IllegalStateException("Already initialized.");
        }
    }

    /**
     * Public accessor.
     *
     * @return an instance of the class.
     */
    public static CachedSecurityAuditProofs getInstance() {

        var result = instance;
        if (result == null) {
            synchronized (CachedSecurityAuditProofs.class) {
                result = instance;
                if (result == null) {
                    instance = result = new CachedSecurityAuditProofs();
                    securityAuditProofs = new ArrayList<>();
                }
            }
        }
        return result;
    }


    public static void setInstance(CachedSecurityAuditProofs instance) {
        CachedSecurityAuditProofs.instance = instance;
    }

    public static List<KademliaData> getSecurityAuditProofs() {
        return securityAuditProofs;
    }

    public static void setSecurityAuditProofs(List<KademliaData> securityAuditProofs) {
        CachedSecurityAuditProofs.securityAuditProofs = securityAuditProofs;
    }

    @Override
    public String toString() {
        return "CachedSecurityAuditProofs{}";
    }
}
