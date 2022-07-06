package io.Adrestus.crypto.bls.utils;



import java.util.List;

public class CommonUtils {

    public static int padCollection(List<List<Integer>> coll, int pad) {
        if(coll.size() > 0) {
            int maxLength = coll.get(0).size();
            for(int i = 1; i < coll.size(); i++) {
                if(maxLength < coll.get(i).size()) {
                    maxLength = coll.get(i).size();
                }
            }
            
            for(int i = 0; i < coll.size(); i++) {
                List<Integer> col = coll.get(i);
                for(int j = col.size(); j < maxLength; j++) {
                    col.add(pad);
                }
            }
            return maxLength;            
        }
        return 0;
    }   
    
}
