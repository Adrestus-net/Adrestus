package io.Adrestus.crypto.bls.utils;

import io.Adrestus.crypto.bls.constants.*;
import io.Adrestus.crypto.bls.model.*;

import java.util.ArrayList;
import java.util.List;

public class MultiSigSlowUtils {
    
    private static FieldElement hashVerkeyForAggregation(VerKey verkey, List<byte[]> allVerkeyBytes) {
        byte[] verkeyBytes = verkey.toBytes();
        
        int totalLen = Constants.VERKEY_DOMAIN_PREFIX.length + verkeyBytes.length;
        
        for(byte[] buf : allVerkeyBytes) {
            totalLen += buf.length;
        }
        
        byte[] result = new byte[totalLen];
        int curLen = 0;
       
        // Add VERKEY_DOMAIN_PREFIX
        System.arraycopy(Constants.VERKEY_DOMAIN_PREFIX, 0, result, curLen, Constants.VERKEY_DOMAIN_PREFIX.length);
        curLen += Constants.VERKEY_DOMAIN_PREFIX.length;        
               
        // Add verkeyBytes
        System.arraycopy(verkeyBytes, 0, result, curLen, verkeyBytes.length);
        curLen += verkeyBytes.length;
        
        // Add each allVerkeyBytes
        for(byte[] buf : allVerkeyBytes) {
            System.arraycopy(buf, 0, result, curLen, buf.length);
            curLen += buf.length;
        }
        
        return FieldElement.fromMsgHash(result);        
    }
    
    public static boolean verify(Signature sig, byte[] msg, VerKey[] verKeys, Params params) {
        VerKey vk = mergeVerKey(verKeys);
        return sig.verify(msg, vk, params);
    }
    
    public static Signature mergeSignature(Signature[] sigs, VerKey[] verKeys) {
        
        List<byte[]> allVerKeyBytes = new ArrayList<>();
        for(VerKey verKey: verKeys) {
            allVerKeyBytes.add(verKey.toBytes());
        }
        
        FieldElement[] hs = new FieldElement[sigs.length];
        G2[] g2Arr = new G2[sigs.length];
        
        for(int i = 0; i < sigs.length; i++) {
            Signature sig = sigs[i];
            VerKey verKey = verKeys[i];
            hs[i] = hashVerkeyForAggregation(verKey, allVerKeyBytes);
            g2Arr[i] = new G2(sig.point.value);
        }
        
        G2 point = G2.multiScalarMulVarTime(g2Arr, hs);
        
        return new Signature(point);
    }
    
    public static VerKey mergeVerKey(VerKey[] verKeys) {
        List<byte[]> vkBytes = new ArrayList<>();
        
        for(VerKey vk: verKeys) {
            vkBytes.add(vk.toBytes());
        }
        
        FieldElement[] hs = new FieldElement[verKeys.length];
        G1[] g1Arr = new G1[verKeys.length];
        
        for(int i = 0; i < verKeys.length; i++) {
            VerKey vk = verKeys[i];
            hs[i] = hashVerkeyForAggregation(vk, vkBytes);
            g1Arr[i] = new G1(vk.point);
        }
        
        G1 point = G1.multiScalarMulVarTime(g1Arr, hs);
        
        return new VerKey(point);
    }
}
