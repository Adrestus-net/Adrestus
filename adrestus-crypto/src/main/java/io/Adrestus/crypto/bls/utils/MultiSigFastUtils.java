package io.Adrestus.crypto.bls.utils;

import io.Adrestus.crypto.bls.constants.*;
import io.Adrestus.crypto.bls.model.*;

import java.util.ArrayList;
import java.util.List;

public class MultiSigFastUtils {
    
    private static FieldElement hashpublicKeyForAggregation(BLSPublicKey publicKey, List<byte[]> allpublicKeyBytes) {
        byte[] publicKeyBytes = publicKey.toBytes();
        
        int totalLen = Constants.publicKey_DOMAIN_PREFIX.length + publicKeyBytes.length;
        
        for(byte[] buf : allpublicKeyBytes) {
            totalLen += buf.length;
        }
        
        byte[] result = new byte[totalLen];
        int curLen = 0;
       
        // Add publicKey_DOMAIN_PREFIX
        System.arraycopy(Constants.publicKey_DOMAIN_PREFIX, 0, result, curLen, Constants.publicKey_DOMAIN_PREFIX.length);
        curLen += Constants.publicKey_DOMAIN_PREFIX.length;        
               
        // Add publicKeyBytes
        System.arraycopy(publicKeyBytes, 0, result, curLen, publicKeyBytes.length);
        curLen += publicKeyBytes.length;
        
        // Add each allpublicKeyBytes
        for(byte[] buf : allpublicKeyBytes) {
            System.arraycopy(buf, 0, result, curLen, buf.length);
            curLen += buf.length;
        }
        
        return FieldElement.fromMsgHash(result);        
    }
    
    public static boolean verify(Signature sig, byte[] msg, BLSPublicKey[] publicKeys, Params params) {
        BLSPublicKey vk = mergepublicKey(publicKeys);
        return BLSSignature.verify(sig,msg, vk, params);
    }

    public static boolean verify(Signature sig, byte[] msg, BLSPublicKey[] publicKeys) {
        BLSPublicKey vk = mergepublicKey(publicKeys);
        return BLSSignature.verify(sig,msg, vk);
    }
    
    public static Signature mergeSignature(Signature[] sigs, BLSPublicKey[] publicKeys) {
        
        List<byte[]> allpublicKeyBytes = new ArrayList<>();
        for(BLSPublicKey publicKey: publicKeys) {
            allpublicKeyBytes.add(publicKey.toBytes());
        }
        
        FieldElement[] hs = new FieldElement[sigs.length];
        G2Point[] g2Arr = new G2Point[sigs.length];
        
        for(int i = 0; i < sigs.length; i++) {
            Signature sig = sigs[i];
            BLSPublicKey BLSPublicKey = publicKeys[i];
            hs[i] = hashpublicKeyForAggregation(BLSPublicKey, allpublicKeyBytes);
            g2Arr[i] = new G2Point(sig.getPoint().getValue());
        }
        
        G2Point point = G2Point.multiScalarMulVarTime(g2Arr, hs);
        
        return new Signature(point);
    }
    
    public static BLSPublicKey mergepublicKey(BLSPublicKey[] publicKeys) {
        List<byte[]> vkBytes = new ArrayList<>();
        
        for(BLSPublicKey vk: publicKeys) {
            vkBytes.add(vk.toBytes());
        }
        
        FieldElement[] hs = new FieldElement[publicKeys.length];
        G1Point[] g1Arr = new G1Point[publicKeys.length];
        
        for(int i = 0; i < publicKeys.length; i++) {
            BLSPublicKey vk = publicKeys[i];
            hs[i] = hashpublicKeyForAggregation(vk, vkBytes);
            g1Arr[i] = new G1Point(vk.getPoint());
        }
        
        G1Point point = G1Point.multiScalarMulVarTime(g1Arr, hs);
        
        return new BLSPublicKey(point);
    }
}
