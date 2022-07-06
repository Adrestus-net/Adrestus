package io.Adrestus.crypto.bls.model;

public class G2LookupTable {

    private G2[] table;
    
    public G2LookupTable(G2 A) {        
        G2[] Ai = new G2[8];
        
        Ai[0] = new G2(A);
        G2 A2 = new G2(A);
        A2.dbl();
        
        for(int i = 0; i < 7; i++) {
            Ai[i+1] = new G2(Ai[i]);
            Ai[i+1].add(A2);
        }
        this.table = Ai;
    }
    
    public G2 select(int x) {
        return this.table[x/2];
    }
}
